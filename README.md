# Habit Tracker

[![CI](https://github.com/75py/habit-tracker/actions/workflows/ci.yml/badge.svg)](https://github.com/75py/habit-tracker/actions/workflows/ci.yml)

Kotlin Multiplatform と Compose Multiplatform で構築されたクロスプラットフォーム習慣追跡アプリケーションです。

## プロジェクト概要

Habit Tracker は、ユーザーが良い習慣を構築し維持することを支援する現代的なモバイルアプリケーションです。カスタム習慣の作成、進捗の追跡、様々な期間での継続性の可視化を可能にします。

## 使用技術スタック

- **Kotlin Multiplatform (KMP) 2.1.0**: Android と iOS プラットフォーム間でのビジネスロジック共有
- **Compose Multiplatform 1.8.1**: ネイティブユーザーインターフェース作成のための統一UIフレームワーク
- **Room Database 2.7.1**: 型安全なデータベースレイヤーによるローカルデータ永続化
- **Koin 4.0.4**: アプリケーション依存関係管理のための軽量依存性注入フレームワーク
- **kotlinx.datetime 0.6.1**: クロスプラットフォーム日時処理
- **Napier 2.7.1**: 一貫したデバッグとエラートラッキングのためのクロスプラットフォームログライブラリ
- **Navigation Compose 2.7.0**: 画面間ナビゲーション
- **MockK 1.13.13**: Kotlin ネイティブなモックライブラリによるテスト

## プロジェクト構造

Android と iOS をターゲットとする Kotlin Multiplatform プロジェクトです。

### ディレクトリ構成

* `/composeApp` - Compose Multiplatform アプリケーションの共有コードを含みます
  - `commonMain` - 全プラットフォーム共通のコード
  - `androidMain` - Android 固有の実装
  - `iosMain` - iOS 固有の実装
  - `commonTest` - 共通テストコード
  - `androidUnitTest` - Android ユニットテスト

* `/iosApp` - iOS アプリケーションのエントリーポイント
* `/docs` - 詳細なプロジェクトドキュメント

### ソースコード構成

```
composeApp/src/commonMain/kotlin/com/nagopy/kmp/habittracker/
├── data/           # データレイヤー (Room, Repository実装)
├── domain/         # ドメインレイヤー (Use Cases, Repository interface, Entity)
├── presentation/   # プレゼンテーションレイヤー (UI, ViewModel)
├── di/            # 依存性注入 (Koin モジュール)
├── notification/   # 通知機能
└── util/          # ユーティリティ
```

## アーキテクチャ

本アプリケーションは **三層アーキテクチャ** パターンに従い、関心の分離、保守性、テスト可能性を確保しています。

### レイヤー構成

```
┌─────────────────────────────────────┐
│        プレゼンテーション層           │
│     (UI, ViewModel, Navigation)     │
└─────────────────────────────────────┘
                    │
                    ▼
┌─────────────────────────────────────┐
│           ドメイン層                 │
│   (ビジネスロジック, Use Cases,      │
│    Entity, Repository Interface)    │
└─────────────────────────────────────┘
                    │
                    ▼
┌─────────────────────────────────────┐
│            データ層                  │
│  (Repository実装, データソース,      │
│   データベース, ネットワーク, マッパー) │
└─────────────────────────────────────┘
```

### 主要コンポーネント

- **プレゼンテーション層**: MVVM パターンによる UI と状態管理
- **ドメイン層**: ビジネスロジックと Use Cases
- **データ層**: Room データベースと Repository パターン実装

詳細なアーキテクチャ情報は [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md) を参照してください。

## 開発環境のセットアップ

### 必要な環境

- **Android Studio**: Giraffe 以降
- **Xcode**: 15.0 以降 (iOS 開発の場合)
- **Kotlin**: 2.1.0
- **JDK**: 17 以降

### プロジェクトのビルド

1. **リポジトリのクローン**
   ```bash
   git clone https://github.com/75py/habit-tracker.git
   cd habit-tracker
   ```

2. **Android 開発**
   - Android Studio でプロジェクトを開く
   - Android Studio から直接ビルドと実行
   - またはコマンドラインから:
     ```bash
     ./gradlew clean build
     ./gradlew assembleDebug
     ./gradlew installDebug
     ```

3. **iOS 開発**
   - `iosApp` フォルダを Xcode で開く
   - Xcode からプロジェクトを実行
   - iOS 14.0+ のデプロイメントターゲットが必要

### テストの実行

```bash
# Android ユニットテスト
./gradlew testDebugUnitTest

# 全テスト
./gradlew test

# リント実行
./gradlew lint
```

詳細なビルドコマンドについては [docs/DEVELOPMENT_COMMANDS.md](docs/DEVELOPMENT_COMMANDS.md) を参照してください。

## 主な機能

- **習慣管理**: カスタム習慣の作成・編集・削除
- **タスク実行**: 時刻ベースのタスクリストと完了機能
- **進捗追跡**: 日別・週別の習慣継続状況の可視化
- **クロスプラットフォーム**: Android と iOS での一貫したユーザー体験
- **ローカルデータ**: Room データベースによる高速なオフライン動作
- **通知機能**: プラットフォーム固有の最適化されたリマインダー

## ドキュメント

プロジェクトの詳細なドキュメントは `/docs` ディレクトリにあります：

- [📚 ドキュメント索引](docs/README.md) - 全ドキュメントの概要とナビゲーション
- [🏗️ アーキテクチャ](docs/ARCHITECTURE.md) - 三層アーキテクチャの詳細設計
- [⚙️ 開発コマンド](docs/DEVELOPMENT_COMMANDS.md) - ビルド、テスト、メンテナンスコマンド
- [📝 コーディング規約](docs/CODING_STANDARDS.md) - ログ要件とベストプラクティス
- [🧪 テスト戦略](docs/TESTING.md) - テスト基準と戦略
- [📱 画面仕様](docs/SPECIFICATIONS.md) - UI/UX 仕様書

## Firebase App Distribution

Firebase App Distribution を使用してテストアプリ（Android/iOS）を配信できます。

- **セットアップ**: [Firebase App Distribution セットアップガイド](docs/FIREBASE_APP_DISTRIBUTION.md)
- **GitHub Actions**: 手動ワークフローでテストアプリを配信（プラットフォーム選択可能）
- **ローカル実行**: `./scripts/firebase-distribute.sh [platform]` スクリプトで開発者PCから配信
- **fastlane ベース**: アプリ本体の Gradle 設定に影響しない外部実装

## スクリーンショット

![App Screenshots](docs/images/screenshots-placeholder.png)
*習慣追跡アプリの主要機能を示すスクリーンショットをここに追加予定*

**主要機能:**
- 時刻ベースのタスクリストを含む「今日のタスク」画面
- カスタムカラーとスケジューリング機能付きの習慣作成
- リアルタイム更新によるタスク完了機能

## 貢献・開発

このプロジェクトへの貢献を歓迎します。詳細な開発ガイドラインについては、以下を参照してください：

- [コーディング規約](docs/CODING_STANDARDS.md)
- [テスト戦略](docs/TESTING.md)
- [開発コマンド](docs/DEVELOPMENT_COMMANDS.md)

## 技術的な詳細

### プラットフォーム対応
- **Android**: Min SDK 24, Target SDK 35, JVM 17
- **iOS**: iosX64, iosArm64, iosSimulatorArm64 with static framework

### 参考リンク
- [Kotlin Multiplatform について詳しく学ぶ](https://www.jetbrains.com/help/kotlin-multiplatform-dev/get-started.html)

---

**最終更新日**: 2025-01-20

*AI アシスタント設定については、プロジェクトルートの [CLAUDE.md](CLAUDE.md) を参照してください。*