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
        BGTaskScheduler.shared.register(
            forTaskWithIdentifier: taskIdentifier,
            using: nil
        ) { task in
            self.handleNotificationRefresh(task: task as! BGAppRefreshTask)
        }
    }
    
    /// Schedules the next background task
    func scheduleBackgroundRefresh() {
        let request = BGAppRefreshTaskRequest(identifier: taskIdentifier)
        request.earliestBeginDate = Date(timeIntervalSinceNow: 60 * 60) // 1 hour from now
        
        do {
            try BGTaskScheduler.shared.submit(request)
            print("Background notification refresh task scheduled")
        } catch {
            print("Failed to schedule background task: \(error)")
        }
    }
    
    /// Handles the background notification refresh task
    private func handleNotificationRefresh(task: BGAppRefreshTask) {
        print("Performing background notification refresh")
        
        // Schedule the next refresh
        scheduleBackgroundRefresh()
        
        // Perform the notification refresh
        task.expirationHandler = {
            task.setTaskCompleted(success: false)
        }
        
        // Call Kotlin notification refresh
        IOSBackgroundNotificationHelper.shared.performBackgroundRefresh()
        
        // Mark task as completed
        task.setTaskCompleted(success: true)
    }
    
    /// Cancels pending background tasks
    func cancelBackgroundTasks() {
        BGTaskScheduler.shared.cancel(taskWithIdentifier: taskIdentifier)
    }
}