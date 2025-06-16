# Firebase App Distribution セットアップガイド

このガイドでは、Firebase App Distributionを使用してHabitTrackerアプリのテストビルドを配信するためのセットアップ手順を説明します。

## 前提条件

1. Firebaseプロジェクトの作成が必要
2. Firebase App Distributionの有効化が必要
3. Androidアプリの登録が必要

## 手動セットアップ手順

### 1. Firebaseプロジェクトの作成

1. [Firebase Console](https://console.firebase.google.com/)にアクセス
2. 「プロジェクトを追加」をクリック
3. プロジェクト名を入力（例：`habit-tracker-distribution`）
4. Google Analyticsの設定を完了してプロジェクトを作成

### 2. Androidアプリの登録

1. Firebase Console でプロジェクトを開く
2. 「アプリを追加」→「Android」を選択
3. パッケージ名に `com.nagopy.kmp.habittracker` を入力
4. アプリの登録を完了

### 3. Firebase App Distributionの有効化

1. Firebase Console の左メニューから「App Distribution」を選択
2. 「使ってみる」をクリックしてサービスを有効化

### 4. サービスアカウントの作成

1. [Google Cloud Console](https://console.cloud.google.com/)にアクセス
2. 先ほど作成したFirebaseプロジェクトを選択
3. 「IAM と管理」→「サービス アカウント」を選択
4. 「サービス アカウントを作成」をクリック
5. 名前を入力（例：`app-distribution-service`）
6. 「Firebase App Distribution 管理者 SDK サービス エージェント」ロールを追加
7. 「完了」をクリック

### 5. サービスアカウントキーの生成

1. 作成したサービスアカウントの「操作」メニューから「キーを管理」を選択
2. 「キーを追加」→「新しいキーを作成」を選択
3. 「JSON」形式を選択してキーをダウンロード
4. ダウンロードしたファイルを安全な場所に保存

### 6. google-services.json の設定

1. Firebase Console でプロジェクトを開く
2. プロジェクト設定（歯車アイコン）→「全般」タブを選択
3. 「マイアプリ」セクションで作成したAndroidアプリを選択
4. 「google-services.json」をダウンロード
5. ファイルを `composeApp/google-services.json` として配置

### 7. テスターグループの作成

1. Firebase Console の「App Distribution」を選択
2. 「テスター」タブを選択
3. 「グループを作成」をクリック
4. グループ名に `internal-testers` を入力
5. テスターのメールアドレスを追加

## GitHub Actions の設定

### 1. GitHub Secrets の追加

リポジトリの Settings → Secrets and variables → Actions から以下のシークレットを追加：

- `FIREBASE_APP_ID`: Firebase Console のプロジェクト設定で確認できるアプリID
- `GOOGLE_SERVICES_JSON`: ダウンロードした `google-services.json` ファイルの内容
- `FIREBASE_SERVICE_ACCOUNT_JSON`: ダウンロードしたサービスアカウントキーJSONファイルの内容

### 2. ワークフローの実行

1. GitHub リポジトリの「Actions」タブを開く
2. 「Firebase App Distribution」ワークフローを選択
3. 「Run workflow」をクリック
4. 必要に応じてリリースノートやテスターグループを指定
5. 「Run workflow」を実行

## ローカル実行

### 1. 必要なファイルの配置

- `composeApp/google-services.json`: Firebase設定ファイル
- `firebase-service-account.json`: サービスアカウントキー（プロジェクトルートに配置）

### 2. Gradle プロパティの設定

`local.properties` ファイルまたは環境変数に以下を設定：

```properties
FIREBASE_APP_ID=your-firebase-app-id
FIREBASE_SERVICE_ACCOUNT_FILE=firebase-service-account.json
TESTER_GROUPS=internal-testers
RELEASE_NOTES=ローカルからのテストビルド
```

### 3. 実行コマンド

```bash
# APKをビルドしてFirebase App Distributionにアップロード
./gradlew assembleDebug appDistributionUploadDebug
```

## セキュリティの注意事項

1. **機密ファイルの管理**
   - `google-services.json` ファイルは `.gitignore` に追加済み
   - サービスアカウントキーは絶対にコミットしない
   - GitHub Secrets で機密情報を管理する

2. **アクセス制御**
   - サービスアカウントには必要最小限の権限のみ付与
   - テスターグループのメンバーを適切に管理

3. **ファイルの削除**
   - CI/CDパイプライン実行後は機密ファイルを削除
   - ローカル環境でも不要になったファイルは削除

## トラブルシューティング

### よくある問題

1. **google-services.json が見つからない**
   - ファイルが正しい場所（`composeApp/google-services.json`）に配置されているか確認
   - ファイルの形式が正しいか確認

2. **サービスアカウントの権限不足**
   - サービスアカウントに「Firebase App Distribution 管理者 SDK サービス エージェント」ロールが付与されているか確認

3. **アプリIDが間違っている**
   - Firebase Console で正しいアプリIDを確認
   - パッケージ名が一致しているか確認

### ログの確認

```bash
# Gradleの詳細ログを確認
./gradlew assembleDebug appDistributionUploadDebug --info
```

## 参考リンク

- [Firebase App Distribution](https://firebase.google.com/docs/app-distribution)
- [Firebase App Distribution Gradle Plugin](https://firebase.google.com/docs/app-distribution/android/distribute-gradle)
- [Google Cloud サービスアカウント](https://cloud.google.com/iam/docs/service-accounts)