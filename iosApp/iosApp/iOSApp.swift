import SwiftUI
import ComposeApp
import PushKit

class AppDelegate: NSObject, UIApplicationDelegate {

  func application(_ application: UIApplication,
                   didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey : Any]? = nil) -> Bool {

      _ = PushkitRegistryKt.Instance(queue: DispatchQueue.main, pushType: PKPushType.voIP.rawValue)
      CallManagerKt.initializeCallManagerInstance(queue: DispatchQueue.main)

      return true
  }
    
  func application(_ application: UIApplication, didRegisterForRemoteNotificationsWithDeviceToken deviceToken: Data) {

  }

  func application(_ application: UIApplication, didReceiveRemoteNotification userInfo: [AnyHashable : Any]) async -> UIBackgroundFetchResult {

      return UIBackgroundFetchResult.newData
  }
}

@main
struct iOSApp: App {
    @UIApplicationDelegateAdaptor(AppDelegate.self) var delegate

    var body: some Scene {
        WindowGroup {
            ContentView()
        }
    }
}
