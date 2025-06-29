# Firebase App Distribution セットアップガイド

このガイドでは、Firebase App Distributionを使用してHabitTrackerアプリ（Android/iOS）のテストビルドを配信するためのセットアップ手順を説明します。

## 配信方法の違い

- **Android**: GitHub Actionsとローカルのどちらでも配信可能
- **iOS**: ローカル実行のみ（GitHub ActionsはLinuxのためiOSビルド不可）

## 前提条件

1. Firebaseプロジェクトの作成が必要
2. Firebase App Distributionの有効化が必要
3. Android/iOSアプリの登録が必要
4. fastlaneのインストールが必要
5. **iOS配信用**: macOS環境とXcode設定が必要

## 手動セットアップ手順

### 1. Firebaseプロジェクトの作成

1. [Firebase Console](https://console.firebase.google.com/)にアクセス
2. 「プロジェクトを追加」をクリック
3. プロジェクト名を入力（例：`habit-tracker-distribution`）
4. Google Analyticsの設定を完了してプロジェクトを作成

### 2. Androidアプリの登録

1. Firebase Console でプロジェクトを開く
2. 「アプリを追加」→「Android」を選択
3. パッケージ名に `com.nagopy.android.habittracker` を入力
4. アプリの登録を完了

### 3. iOSアプリの登録

1. Firebase Console でプロジェクトを開く
2. 「アプリを追加」→「iOS」を選択
3. バンドルIDに `com.nagopy.ios.habittracker` を入力
4. アプリの登録を完了

### 4. Firebase App Distributionの有効化

1. Firebase Console の左メニューから「App Distribution」を選択
2. 「使ってみる」をクリックしてサービスを有効化

### 5. サービスアカウントの作成

1. [Google Cloud Console](https://console.cloud.google.com/)にアクセス
2. 先ほど作成したFirebaseプロジェクトを選択
3. 「IAM と管理」→「サービス アカウント」を選択
4. 「サービス アカウントを作成」をクリック
5. 名前を入力（例：`app-distribution-service`）
6. 「Firebase App Distribution 管理者 SDK サービス エージェント」ロールを追加
7. 「完了」をクリック

### 6. サービスアカウントキーの生成

1. 作成したサービスアカウントの「操作」メニューから「キーを管理」を選択
2. 「キーを追加」→「新しいキーを作成」を選択
3. 「JSON」形式を選択してキーをダウンロード
4. ダウンロードしたファイルを安全な場所に保存

### 7. Android 設定ファイルのダウンロード

1. Firebase Console でプロジェクトを開く
2. プロジェクト設定（歯車アイコン）→「全般」タブを選択
3. 「マイアプリ」セクションで作成したAndroidアプリを選択
4. 「google-services.json」をダウンロード
5. ファイルを `composeApp/google-services.json` として配置

### 8. iOS 設定ファイルのダウンロード

1. Firebase Console でプロジェクトを開く
2. プロジェクト設定（歯車アイコン）→「全般」タブを選択
3. 「マイアプリ」セクションで作成したiOSアプリを選択
4. 「GoogleService-Info.plist」をダウンロード
5. ファイルを `iosApp/iosApp/GoogleService-Info.plist` として配置

### 9. テスターグループの作成

1. Firebase Console の「App Distribution」を選択
2. 「テスター」タブを選択
3. 「グループを作成」をクリック
4. グループ名に `internal-testers` を入力
5. テスターのメールアドレスを追加

## fastlane の設定

### 1. fastlane のインストール

```bash
# Rubyがインストールされていることを確認
ruby --version

# fastlane をインストール
gem install fastlane

# プロジェクトディレクトリで依存関係をインストール
cd /path/to/habit-tracker
fastlane install_plugins
```

## GitHub Actions の設定（Android のみ）

**注意**: GitHub ActionsはLinux環境で実行されるため、Androidアプリのみ配信可能です。iOSアプリはローカル環境での配信をご利用ください。

### 1. GitHub Environment の作成

1. リポジトリの Settings → Environments を開く
2. 「New environment」をクリック
3. 環境名に `firebase-distribution` を入力
4. 「Configure environment」をクリック

### 2. Environment Secrets の追加

作成した `firebase-distribution` 環境で以下のシークレットを追加：

**Android用:**
- `FIREBASE_APP_ID`: Firebase Console のAndroidアプリのアプリID
- `GOOGLE_SERVICES_JSON`: ダウンロードした `google-services.json` ファイルの内容

**共通:**
- `FIREBASE_SERVICE_ACCOUNT_JSON`: ダウンロードしたサービスアカウントキーJSONファイルの内容

**Environment Secrets を使用する利点:**
- より細かいアクセス制御が可能
- 環境ごとに異なる設定を管理可能
- プロテクションルールを設定可能

### 3. ワークフローの実行

1. GitHub リポジトリの「Actions」タブを開く
2. 「Firebase App Distribution」ワークフローを選択
3. 「Run workflow」をクリック
4. 必要に応じてリリースノートやテスターグループを指定
5. 「Run workflow」を実行

## ローカル実行

### 1. 必要なファイルの配置

**Android用:**
- `composeApp/google-services.json`: Firebase Android設定ファイル

**iOS用:**
- `iosApp/iosApp/GoogleService-Info.plist`: Firebase iOS設定ファイル

**共通:**
- `firebase-service-account.json`: サービスアカウントキー（プロジェクトルートに配置）

### 2. iOS用の追加要件

**重要**: iOS配信にはmacOS環境とXcode設定が必要です：

1. **必要な証明書とプロファイル**:
   - P12形式の配布証明書（AdHoc配信用）
   - AdHoc配信用のProvisioning Profile
   - Apple Developer Team ID

2. **証明書の準備**:
   ```bash
   # Keychain Accessから証明書をP12形式でエクスポート
   # 1. Keychain Accessを開く
   # 2. "My Certificates"カテゴリから配布証明書を選択
   # 3. 右クリック → "Export..."
   # 4. P12形式で保存し、パスワードを設定
   ```

3. **Provisioning Profileの準備**:
   ```bash
   # Apple Developer Portalからダウンロード
   # 1. https://developer.apple.com/account にアクセス
   # 2. Certificates, Identifiers & Profiles → Profiles
   # 3. AdHoc用のプロファイルをダウンロード
   ```

4. **Team IDの確認**:
   ```bash
   # Apple Developer Portalで確認
   # Membership → Team ID セクションに記載
   ```

5. **AdHoc配信設定**:
   - iOS配信はAdHoc形式で行われます
   - テスト端末のUDIDを事前にApple Developer Consoleに登録する必要があります

### 3. 環境変数の設定

環境変数設定ファイル（`.env.firebase`）を使用することを推奨します：

```bash
# .env.firebase.template をコピーして設定
cp .env.firebase.template .env.firebase

# .env.firebase を編集して必要な値を設定
vim .env.firebase
```

**直接環境変数を設定する場合**:

```bash
# Android用
export FIREBASE_APP_ID=your-android-firebase-app-id

# iOS用
export FIREBASE_APP_ID_IOS=your-ios-firebase-app-id
export TEAM_ID=your-apple-team-id  # 例: ABC123DEF
export P12_PATH=/path/to/your/certificate.p12
export P12_PASSWORD=your-p12-password
export PROVISIONING_PROFILE_PATH=/path/to/your/profile.mobileprovision

# 共通
export TESTER_GROUPS=internal-testers
```

### 4. スクリプトの実行

スクリプトは自動的に`.env.firebase`ファイルを読み込みます：

```bash
# Android のみ配信
./scripts/firebase-distribute.sh android "Androidテストビルド"

# iOS のみ配信
./scripts/firebase-distribute.sh ios "iOSテストビルド"

# 両方配信
./scripts/firebase-distribute.sh both "マルチプラットフォームテストビルド"

# デフォルト（both）
./scripts/firebase-distribute.sh

# iOS証明書とプロファイルの確認（デバッグ用）
# .env.firebaseが自動で読み込まれない場合は手動でsourceしてください
source .env.firebase && fastlane ios check_setup
```

**注意**: `firebase-distribute.sh`スクリプトはプロジェクトルートの`.env.firebase`ファイルを自動的に検出して読み込みます。手動で`source`する必要はありません。

## セキュリティの注意事項

1. **機密ファイルの管理**
   - `google-services.json` ファイルは `.gitignore` に追加済み
   - `GoogleService-Info.plist` ファイルは `.gitignore` に追加済み
   - サービスアカウントキーは絶対にコミットしない
   - `.env.firebase` ファイルは `.gitignore` に追加されており、コミットされません
   - P12証明書とProvisioning Profileも機密情報として扱う
   - GitHub Secrets で機密情報を管理する

2. **アクセス制御**
   - サービスアカウントには必要最小限の権限のみ付与
   - テスターグループのメンバーを適切に管理

3. **ファイルの削除**
   - CI/CDパイプライン実行後は機密ファイルを削除
   - ローカル環境でも不要になったファイルは削除

## トラブルシューティング

### よくある問題

1. **設定ファイルが見つからない**
   - Android: ファイルが正しい場所（`composeApp/google-services.json`）に配置されているか確認
   - iOS: ファイルが正しい場所（`iosApp/iosApp/GoogleService-Info.plist`）に配置されているか確認
   - ファイルの形式が正しいか確認

2. **サービスアカウントの権限不足**
   - サービスアカウントに「Firebase App Distribution 管理者 SDK サービス エージェント」ロールが付与されているか確認

3. **アプリIDが間違っている**
   - Firebase Console で正しいアプリIDを確認
   - パッケージ名/バンドルIDが一致しているか確認

4. **fastlane が見つからない**
   - `gem install fastlane` でfastlaneをインストール
   - `fastlane install_plugins` でプラグインをインストール

5. **iOS AdHoc ビルドの問題**
   - Provisioning Profileが正しく設定されているか確認
   - Certificate（証明書）が有効か確認
   - テスト端末のUDIDがApple Developer Consoleに登録されているか確認
   - AdHoc配信用のProvisioning ProfileがFirebase App Distributionと互換性があるか確認
   - Team IDが正しく設定されているか確認
   - P12ファイルのパスとパスワードが正しいか確認
   
   **デバッグ方法**:
   ```bash
   # 証明書とプロファイルの設定確認
   fastlane ios check_setup
   
   # 詳細ログでビルドを実行
   fastlane ios firebase_distribute --verbose
   ```

6. **iOS配信がGitHub Actionsで失敗する**
   - GitHub ActionsはLinux環境のため、iOS配信はサポートされていません
   - iOS配信はmacOS環境でのローカル実行をご利用ください

### ログの確認

```bash
# fastlane の詳細ログを確認
fastlane android firebase_distribute --verbose

# iOS の場合
fastlane ios firebase_distribute --verbose
```

## 参考リンク

- [Firebase App Distribution](https://firebase.google.com/docs/app-distribution)
- [fastlane Firebase App Distribution plugin](https://docs.fastlane.tools/plugins/available-plugins/#firebase_app_distribution)
- [Google Cloud サービスアカウント](https://cloud.google.com/iam/docs/service-accounts)