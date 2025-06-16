#!/bin/bash

# Firebase App Distribution ローカル実行スクリプト
# 使用方法: ./scripts/firebase-distribute.sh [platform] [release-notes]
# platform: android, ios, both (default: both)

# デフォルト値
PLATFORM=${1:-both}
RELEASE_NOTES=${2:-"開発者PCからのテストビルド"}

# 有効なプラットフォームのチェック
if [[ "$PLATFORM" != "android" && "$PLATFORM" != "ios" && "$PLATFORM" != "both" ]]; then
    echo "❌ エラー: 無効なプラットフォーム: $PLATFORM"
    echo "💡 使用可能なプラットフォーム: android, ios, both"
    exit 1
fi

echo "🚀 Firebase App Distribution でアプリを配信します..."
echo "📱 プラットフォーム: $PLATFORM"
echo "📝 リリースノート: $RELEASE_NOTES"
echo "👥 テスターグループ: ${TESTER_GROUPS:-internal-testers}"
echo ""

# 共通設定ファイルのチェック
if [ ! -f "firebase-service-account.json" ]; then
    echo "❌ エラー: firebase-service-account.json が見つかりません"
    echo "📝 セットアップ手順: docs/FIREBASE_APP_DISTRIBUTION.md を参照してください"
    exit 1
fi

# fastlaneの存在チェック
if ! command -v fastlane &> /dev/null; then
    echo "❌ エラー: fastlane がインストールされていません"
    echo "💡 インストール方法: gem install fastlane"
    exit 1
fi

# プラットフォーム別の設定チェックと実行
case $PLATFORM in
    "android")
        # Android設定チェック
        if [ ! -f "composeApp/google-services.json" ]; then
            echo "❌ エラー: composeApp/google-services.json が見つかりません"
            echo "📝 セットアップ手順: docs/FIREBASE_APP_DISTRIBUTION.md を参照してください"
            exit 1
        fi
        
        if [ -z "$FIREBASE_APP_ID" ]; then
            echo "❌ エラー: FIREBASE_APP_ID 環境変数が設定されていません"
            echo "💡 設定例: export FIREBASE_APP_ID=your-firebase-app-id"
            exit 1
        fi
        
        echo "🤖 Android APK を配信します..."
        export FIREBASE_SERVICE_ACCOUNT_FILE="firebase-service-account.json"
        export RELEASE_NOTES="$RELEASE_NOTES"
        fastlane android firebase_distribute release_notes:"$RELEASE_NOTES" groups:"${TESTER_GROUPS:-internal-testers}"
        ;;
        
    "ios")
        # iOS設定チェック
        if [ ! -f "iosApp/iosApp/GoogleService-Info.plist" ]; then
            echo "❌ エラー: iosApp/iosApp/GoogleService-Info.plist が見つかりません"
            echo "📝 セットアップ手順: docs/FIREBASE_APP_DISTRIBUTION.md を参照してください"
            exit 1
        fi
        
        if [ -z "$FIREBASE_APP_ID_IOS" ] && [ -z "$FIREBASE_APP_ID" ]; then
            echo "❌ エラー: FIREBASE_APP_ID_IOS または FIREBASE_APP_ID 環境変数が設定されていません"
            echo "💡 設定例: export FIREBASE_APP_ID_IOS=your-firebase-ios-app-id"
            exit 1
        fi
        
        echo "🍎 iOS アプリを配信します..."
        export FIREBASE_SERVICE_ACCOUNT_FILE="firebase-service-account.json"
        export RELEASE_NOTES="$RELEASE_NOTES"
        fastlane ios firebase_distribute release_notes:"$RELEASE_NOTES" groups:"${TESTER_GROUPS:-internal-testers}"
        ;;
        
    "both")
        # 両方の設定チェック
        ANDROID_OK=true
        IOS_OK=true
        
        if [ ! -f "composeApp/google-services.json" ]; then
            echo "⚠️  警告: composeApp/google-services.json が見つかりません (Android配信をスキップ)"
            ANDROID_OK=false
        fi
        
        if [ -z "$FIREBASE_APP_ID" ]; then
            echo "⚠️  警告: FIREBASE_APP_ID 環境変数が設定されていません (Android配信をスキップ)"
            ANDROID_OK=false
        fi
        
        if [ ! -f "iosApp/iosApp/GoogleService-Info.plist" ]; then
            echo "⚠️  警告: iosApp/iosApp/GoogleService-Info.plist が見つかりません (iOS配信をスキップ)"
            IOS_OK=false
        fi
        
        if [ -z "$FIREBASE_APP_ID_IOS" ] && [ -z "$FIREBASE_APP_ID" ]; then
            echo "⚠️  警告: FIREBASE_APP_ID_IOS 環境変数が設定されていません (iOS配信をスキップ)"
            IOS_OK=false
        fi
        
        if [[ "$ANDROID_OK" == false && "$IOS_OK" == false ]]; then
            echo "❌ エラー: Android と iOS の両方の設定が不完全です"
            echo "📝 セットアップ手順: docs/FIREBASE_APP_DISTRIBUTION.md を参照してください"
            exit 1
        fi
        
        echo "📱 AndroidとiOS両方のアプリを配信します..."
        export FIREBASE_SERVICE_ACCOUNT_FILE="firebase-service-account.json"
        export RELEASE_NOTES="$RELEASE_NOTES"
        fastlane firebase_distribute_all release_notes:"$RELEASE_NOTES" groups:"${TESTER_GROUPS:-internal-testers}"
        ;;
esac

if [ $? -eq 0 ]; then
    echo ""
    echo "✅ 配信が完了しました！"
    echo "🔗 Firebase Console で配信状況を確認できます"
else
    echo ""
    echo "❌ 配信に失敗しました"
    echo "📝 トラブルシューティング: docs/FIREBASE_APP_DISTRIBUTION.md を参照してください"
    exit 1
fi