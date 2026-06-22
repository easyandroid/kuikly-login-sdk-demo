import SwiftUI
import UIKit
import LoginSdk

struct ContentView: View {
    @State private var message = "点击下方按钮进入"
    @State private var loggedIn = false
    @State private var didSetup = false

    var body: some View {
        VStack(spacing: 24) {
            Text("Login SDK Demo")
                .font(.title2)
            Text(message)
                .multilineTextAlignment(.center)
                .padding(.horizontal)
            if loggedIn {
                Text(IosDemoBridge.shared.currentUserSummary())
                    .multilineTextAlignment(.center)
                    .padding(.horizontal)
                Button("退出登录") {
                    IosDemoBridge.shared.logoutDemo {
                        loggedIn = false
                        message = "已退出登录"
                    }
                }
            }
            Button(loggedIn ? "刷新会话" : "进入 / 登录") {
                if IosDemoBridge.shared.isLoggedIn() {
                    loggedIn = true
                    message = IosDemoBridge.shared.currentUserSummary()
                } else {
                    IosDemoBridge.shared.launchLoginDemo { result in
                        message = result
                        loggedIn = result.hasPrefix("登录成功")
                    }
                }
            }
            .buttonStyle(.borderedProminent)
        }
        .padding()
        .onAppear {
            setupIfNeeded()
        }
    }

    private func setupIfNeeded() {
        guard !didSetup else { return }
        didSetup = true
        IosDemoBridge.shared.initDemo()
        if let root = rootViewController() {
            IosDemoBridge.shared.installLoginUi(rootViewController: root)
        }
        loggedIn = IosDemoBridge.shared.isLoggedIn()
        if loggedIn {
            message = IosDemoBridge.shared.currentUserSummary()
        }
    }

    private func rootViewController() -> UIViewController? {
        UIApplication.shared.connectedScenes
            .compactMap { $0 as? UIWindowScene }
            .flatMap { $0.windows }
            .first { $0.isKeyWindow }?
            .rootViewController
    }
}
