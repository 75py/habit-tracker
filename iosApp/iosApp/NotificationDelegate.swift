import Foundation
import UserNotifications
import ComposeApp

@objc public class NotificationDelegate: NSObject, UNUserNotificationCenterDelegate {
    
    // Called when a notification is delivered to a foreground app
    public func userNotificationCenter(_ center: UNUserNotificationCenter, willPresent notification: UNNotification, withCompletionHandler completionHandler: @escaping (UNNotificationPresentationOptions) -> Void) {
        // Show the notification even when app is in foreground
        completionHandler([.alert, .sound, .badge])
        
        // Schedule the next notification in the chain
        scheduleNextNotificationFromDelivery(notification: notification)
    }
    
    // Called when user interacts with a notification
    public func userNotificationCenter(_ center: UNUserNotificationCenter, didReceive response: UNNotificationResponse, withCompletionHandler completionHandler: @escaping () -> Void) {
        // Handle the notification response using existing handler
        IOSNotificationSchedulerHelperKt.handleNotificationResponseFromSwift(response: response)
        
        completionHandler()
    }
    
    private func scheduleNextNotificationFromDelivery(notification: UNNotification) {
        // Extract habit ID from notification identifier and schedule next notification
        let identifier = notification.request.identifier
        IOSNotificationSchedulerHelperKt.scheduleNextNotificationFromDelivery(identifier: identifier)
    }
}