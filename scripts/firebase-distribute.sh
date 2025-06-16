#!/bin/bash

# Firebase App Distribution ローカル実行スクリプト
# 使用方法: ./scripts/firebase-distribute.sh [release-notes]

# 設定ファイルのチェック
if [ ! -f "composeApp/google-services.json" ]; then
    echo "❌ エラー: composeApp/google-services.json が見つかりません"
    echo "📝 セットアップ手順: docs/FIREBASE_APP_DISTRIBUTION.md を参照してください"
    exit 1
fi

if [ ! -f "firebase-service-account.json" ]; then
    echo "❌ エラー: firebase-service-account.json が見つかりません"
    echo "📝 セットアップ手順: docs/FIREBASE_APP_DISTRIBUTION.md を参照してください"
    exit 1
fi

# 環境変数のチェック
if [ -z "$FIREBASE_APP_ID" ]; then
    echo "❌ エラー: FIREBASE_APP_ID 環境変数が設定されていません"
    echo "💡 設定例: export FIREBASE_APP_ID=your-firebase-app-id"
    exit 1
fi

# リリースノートの設定
RELEASE_NOTES=${1:-"開発者PCからのテストビルド"}

echo "🚀 Firebase App Distribution でアプリを配信します..."
echo "📱 アプリID: $FIREBASE_APP_ID"
echo "📝 リリースノート: $RELEASE_NOTES"
echo "👥 テスターグループ: ${TESTER_GROUPS:-internal-testers}"
echo ""

# Gradleタスクの実行
./gradlew assembleDebug appDistributionUploadDebug \
    -PFIREBASE_APP_ID="$FIREBASE_APP_ID" \
    -PFIREBASE_SERVICE_ACCOUNT_FILE="firebase-service-account.json" \
    -PTESTER_GROUPS="${TESTER_GROUPS:-internal-testers}" \
    -PRELEASE_NOTES="$RELEASE_NOTES"

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