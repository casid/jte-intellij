package org.jusecase.jte.intellij.language.convert;

import com.intellij.notification.*;
import com.intellij.openapi.project.Project;

public class JteConvertNotification {
    private static final NotificationGroup STICKY_GROUP = NotificationGroupManager.getInstance().getNotificationGroup("org.jusecase.jte.intellij.language.convert.notification");

    public static void error(Project project, String title, String content) {
        Notification notification = STICKY_GROUP.createNotification(content, NotificationType.ERROR);
        notification.setTitle(title);
        Notifications.Bus.notify(notification, project);
    }
}
