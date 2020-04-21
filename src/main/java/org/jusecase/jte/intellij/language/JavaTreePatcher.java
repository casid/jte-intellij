package org.jusecase.jte.intellij.language;

import com.intellij.psi.impl.source.tree.CompositeElement;
import com.intellij.psi.impl.source.tree.TreeElement;
import com.intellij.psi.templateLanguages.OuterLanguageElement;
import com.intellij.psi.templateLanguages.TreePatcher;
import com.intellij.psi.xml.XmlTokenType;
import org.jetbrains.annotations.NotNull;

public class JavaTreePatcher implements TreePatcher {
    @Override
    public void insert(@NotNull CompositeElement parent, TreeElement anchorBefore, @NotNull OuterLanguageElement toInsert) {
        if(anchorBefore != null) {
            //[mike]
            //Nasty hack. Is used not to insert OuterLanguageElements before the first token of tag.
            //See GeneralJspParsingTest.testHtml6

            if ("package".equals(anchorBefore.getText())) {
                anchorBefore = anchorBefore.getTreeParent();
            }

            anchorBefore.rawInsertBeforeMe((TreeElement)toInsert);
        }
        else parent.rawAddChildren((TreeElement)toInsert);
    }
}
