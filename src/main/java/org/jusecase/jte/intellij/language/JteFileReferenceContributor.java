package org.jusecase.jte.intellij.language;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jusecase.jte.intellij.language.psi.JtePsiFile;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtilCore;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.StandardFileSystems;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.newvfs.BulkFileListener;
import com.intellij.openapi.vfs.newvfs.events.VFileCreateEvent;
import com.intellij.openapi.vfs.newvfs.events.VFileDeleteEvent;
import com.intellij.openapi.vfs.newvfs.events.VFileEvent;
import com.intellij.patterns.PsiJavaPatterns;
import com.intellij.psi.ElementManipulator;
import com.intellij.psi.ElementManipulators;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiFileSystemItem;
import com.intellij.psi.PsiLiteralExpression;
import com.intellij.psi.PsiManager;
import com.intellij.psi.PsiReference;
import com.intellij.psi.PsiReferenceBase;
import com.intellij.psi.PsiReferenceContributor;
import com.intellij.psi.PsiReferenceProvider;
import com.intellij.psi.PsiReferenceRegistrar;
import com.intellij.psi.filters.ElementFilter;
import com.intellij.psi.filters.position.FilterPattern;
import com.intellij.psi.search.FilenameIndex;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.util.IncorrectOperationException;
import com.intellij.util.ProcessingContext;


public class JteFileReferenceContributor extends PsiReferenceContributor {

   @Override
   public void registerReferenceProviders( @NotNull PsiReferenceRegistrar registrar ) {
      registrar.registerReferenceProvider(PsiJavaPatterns.literalExpression().and(new FilterPattern(new JteStringLiteralFilter())),
            FILE_REFERENCE_PROVIDER);
   }

   private static final ConcurrentHashMap<Project, Collection<String>> PROJECT_TO_JTE_DIRS_CACHE = new ConcurrentHashMap<>();
   private static final ConcurrentHashMap<Module, Collection<String>>  MODULE_TO_JTE_DIRS_CACHE  = new ConcurrentHashMap<>();
   private static final PsiReferenceProvider                           FILE_REFERENCE_PROVIDER   = new PsiReferenceProvider() {

      @Override
      public @NotNull PsiReference[] getReferencesByElement( @NotNull PsiElement element, @NotNull ProcessingContext context ) {
         String elementText = getElementText(element);
         if ( !elementText.startsWith("/") ) {
            elementText = "/" + elementText;
         }

         Collection<String> basePaths = getBasePaths(element);

         PsiFile psiFile = null;
         for ( String basePath : basePaths ) {
            VirtualFile file = StandardFileSystems.local().findFileByPath(basePath + elementText);
            psiFile = file == null ? null : PsiManager.getInstance(element.getProject()).findFile(file);
            if ( psiFile != null ) {
               break;
            }
         }

         return psiFile == null ? PsiReference.EMPTY_ARRAY : new PsiReference[] { new JteFileReference(element, psiFile) };
      }

   };

   @NotNull
   private static String getElementText( @NotNull PsiElement element ) {
      return element.getText().substring(1, element.getText().length() - 1);
   }

   @NotNull
   private static Collection<String> getBasePaths( @NotNull PsiElement element ) {
      Project project = element.getProject();
      Module module = getModule(element);
      List<String> paths = new ArrayList<>();
      paths.addAll(getJteDirPaths(module));
      paths.addAll(getJteDirPaths(project));
      paths.add(project.getBasePath());
      return paths;
   }

   @Nullable
   private static Module getModule( @NotNull PsiElement element ) {
      return ModuleUtilCore.findModuleForPsiElement(element);
   }

   private static Collection<String> getJteDirPaths( Project project ) {
      return PROJECT_TO_JTE_DIRS_CACHE.computeIfAbsent(project, p -> {
         PsiFileSystemItem[] jteDirectories = FilenameIndex.getFilesByName(p, "jte", GlobalSearchScope.projectScope(p), true);
         return Arrays.stream(jteDirectories).map(d -> d.getVirtualFile().getPath()).collect(Collectors.toList());
      });
   }

   private static Collection<String> getJteDirPaths( Module module ) {
      return MODULE_TO_JTE_DIRS_CACHE.computeIfAbsent(module, m -> {
         PsiFileSystemItem[] jteDirectories = FilenameIndex.getFilesByName(m.getProject(), "jte", GlobalSearchScope.moduleScope(m), true);
         return Arrays.stream(jteDirectories).map(d -> d.getVirtualFile().getPath()).collect(Collectors.toList());
      });
   }

   public static class JteFileReference extends PsiReferenceBase.Immediate<PsiElement> {

      public JteFileReference( @NotNull PsiElement element, PsiElement resolveTo ) {
         super(element, resolveTo);
      }

      public PsiElement handleElementRename( @NotNull String newFileName ) throws IncorrectOperationException {
         final PsiElement element = getElement();
         ElementManipulator<PsiElement> manipulator = ElementManipulators.getManipulator(element);
         if ( manipulator != null ) {
            String oldElementText = getElementText(element);
            if ( oldElementText.contains("/") ) {
               newFileName = oldElementText.substring(0, oldElementText.lastIndexOf('/')) + "/" + newFileName;
            }
            return manipulator.handleContentChange(element, getRangeInElement(), newFileName);
         }
         return element;
      }

      @Override
      public PsiElement bindToElement( @NotNull PsiElement element ) throws IncorrectOperationException {
         if ( element instanceof JtePsiFile ) {
            String newPath = ((JtePsiFile)element).getVirtualFile().getPath();
            for ( String basePath : getBasePaths(element) ) {
               int basePathIndex = basePath == null ? -1 : newPath.indexOf(basePath);
               if ( basePathIndex >= 0 ) {
                  String newRelativePath = newPath.substring(basePathIndex + basePath.length() + 1);
                  if ( getElementText(getElement()).startsWith("/") ) {
                     newRelativePath = "/" + newRelativePath;
                  }
                  ElementManipulator<PsiElement> manipulator = ElementManipulators.getManipulator(getElement());
                  if ( manipulator != null ) {
                     return manipulator.handleContentChange(getElement(), getRangeInElement(), newRelativePath);
                  }
               }
            }
         }
         return element;
      }
   }

   public static class JteStringLiteralFilter implements ElementFilter {

      public boolean isAcceptable( Object element, PsiElement psiElement ) {
         PsiLiteralExpression literalExpression = (PsiLiteralExpression)element;
         Object value = literalExpression.getValue();
         if (!(value instanceof String)) {
            return false;
         }

         String stringValue = (String)value;
         return stringValue.endsWith(".jte");
      }

      public boolean isClassAcceptable( Class aClass ) {
         return true;
      }
   }

   public static class FileChangeListener implements BulkFileListener {

      @Override
      public void after( @NotNull List<? extends VFileEvent> events ) {
         for ( VFileEvent event: events ){
            if(event instanceof VFileCreateEvent || event instanceof VFileDeleteEvent ){
               if(event.getFile() != null && event.getFile().getName().equals("jte")){
                  PROJECT_TO_JTE_DIRS_CACHE.clear();
                  MODULE_TO_JTE_DIRS_CACHE.clear();
               }
            }
         }
      }
   }
}
