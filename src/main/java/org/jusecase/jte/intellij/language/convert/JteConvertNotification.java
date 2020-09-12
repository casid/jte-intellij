package org.jusecase.jte.intellij.language.convert;

import com.intellij.notification.*;
import com.intellij.openapi.project.Project;

public class JteConvertNotification {
    private static final NotificationGroup STICKY_GROUP = new NotificationGroup("org.jusecase.jte.intellij.language.convert.notification", NotificationDisplayType.STICKY_BALLOON, true);

    public static void error(Project project, String title, String content) {
        Notification notification = STICKY_GROUP.createNotification(title, null, content, NotificationType.ERROR);
        Notifications.Bus.notify(notification, project);
    }
}
