#!/bin/bash

# Firebase App Distribution ローカル実行スクリプト
# 使用方法: ./scripts/firebase-distribute.sh [platform] [release-notes]
# platform: android, ios, both (default: both)

# スクリプトのディレクトリを取得
SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
PROJECT_ROOT="$( cd "$SCRIPT_DIR/.." && pwd )"

# .env.firebase ファイルを読み込む
if [ -f "$PROJECT_ROOT/.env.firebase" ]; then
    echo "📋 .env.firebase から環境変数を読み込んでいます..."
    source "$PROJECT_ROOT/.env.firebase"
elif [ -f ".env.firebase" ]; then
    echo "📋 .env.firebase から環境変数を読み込んでいます..."
    source ".env.firebase"
else
    echo "⚠️  警告: .env.firebase ファイルが見つかりません"
    echo "💡 ヒント: .env.firebase.template をコピーして設定してください"
    echo "   cp .env.firebase.template .env.firebase"
    echo ""
fi

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

# fastlaneの存在チェック
if ! command -v fastlane &> /dev/null; then
    echo "❌ エラー: fastlane がインストールされていません"
    echo "💡 インストール方法: gem install fastlane"
    exit 1
fi

# サービスアカウントファイルの設定（.env.firebaseで上書きされない場合のデフォルト値）
export FIREBASE_SERVICE_ACCOUNT_FILE="${FIREBASE_SERVICE_ACCOUNT_FILE:-firebase-service-account.json}"
export RELEASE_NOTES="$RELEASE_NOTES"

# 必要な環境変数をすべてエクスポート（fastlaneに渡すため）
export FIREBASE_APP_ID
export FIREBASE_APP_ID_IOS
export TEAM_ID
export P12_PATH
export P12_PASSWORD
export PROVISIONING_PROFILE_PATH
export TESTER_GROUPS

# プラットフォーム別にfastlaneを実行
case $PLATFORM in
    "android")
        echo "🤖 Android APK を配信します..."
        fastlane android firebase_distribute release_notes:"$RELEASE_NOTES" groups:"${TESTER_GROUPS:-internal-testers}"
        ;;
    "ios")
        echo "🍎 iOS アプリを配信します..."
        fastlane ios firebase_distribute release_notes:"$RELEASE_NOTES" groups:"${TESTER_GROUPS:-internal-testers}"
        ;;
    "both")
        echo "📱 AndroidとiOS両方のアプリを配信します..."
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