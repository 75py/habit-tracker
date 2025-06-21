import Foundation
import BackgroundTasks
import ComposeApp

/// Manages background task scheduling for notification refresh
class BackgroundTaskManager {
    static let shared = BackgroundTaskManager()
    
    private let taskIdentifier = "com.nagopy.habittracker.notification-refresh"
    
    private init() {}
    
    /// Registers the background task handler
    func registerBackgroundTasks() {
        print("Registering background task handler for identifier: \(taskIdentifier)")
        BGTaskScheduler.shared.register(
            forTaskWithIdentifier: taskIdentifier,
            using: nil
        ) { task in
            print("Background task triggered: \(task.identifier)")
            self.handleNotificationRefresh(task: task as! BGAppRefreshTask)
        }
        print("Background task handler registration completed")
    }
    
    /// Schedules the next background task
    func scheduleBackgroundRefresh() {
        print("Attempting to schedule background refresh task...")
        let request = BGAppRefreshTaskRequest(identifier: taskIdentifier)
        request.earliestBeginDate = Date(timeIntervalSinceNow: 60 * 60) // 1 hour from now
        
        print("Earliest begin date set to: \(request.earliestBeginDate?.description ?? "nil")")
        
        do {
            try BGTaskScheduler.shared.submit(request)
            print("Background notification refresh task scheduled successfully")
        } catch {
            print("Failed to schedule background task: \(error)")
        }
    }
    
    /// Handles the background notification refresh task
    private func handleNotificationRefresh(task: BGAppRefreshTask) {
        print("Starting background notification refresh task: \(task.identifier)")
        print("Task expiration date: \(task.expirationDate?.description ?? "nil")")
        
        // Schedule the next refresh
        print("Scheduling next background refresh...")
        scheduleBackgroundRefresh()
        
        // Perform the notification refresh
        task.expirationHandler = {
            print("Background task expired before completion")
            task.setTaskCompleted(success: false)
        }
        
        // Call Kotlin notification refresh
        print("Calling Kotlin background notification refresh...")
        IOSBackgroundNotificationHelper.shared.performBackgroundRefresh()
        
        // Mark task as completed
        print("Background notification refresh task completed successfully")
        task.setTaskCompleted(success: true)
    }
    
    /// Cancels pending background tasks
    func cancelBackgroundTasks() {
        print("Cancelling background tasks for identifier: \(taskIdentifier)")
        BGTaskScheduler.shared.cancel(taskWithIdentifier: taskIdentifier)
        print("Background tasks cancelled")
    }
}