# SDV News Backend - Claude Code ルール

## プロジェクト概要
日本国内をメインとする自動車関連のニュースを収集・配信するSpring Bootバックエンド。

## 技術スタック
- Java 21 / Spring Boot 3.3.x
- Gradle (Kotlin DSL)
- Supabase (PostgreSQL)
- JJWT 0.12.x（JWT検証）
- Rome（RSSパース）

## パッケージ構成
```
com.example.sdvnews
├── security/   JWT認証基盤
├── article/    Articleドメイン（Entity, Repository, Service, Controller）
├── bookmark/   Bookmarkドメイン（Entity, Repository, Service, Controller）
└── batch/      RSSバッチ処理
```

## 設計方針
- ステートレス認証（Supabase ES256 JWT、JWKS エンドポイントで公開鍵検証）
- バッチエンドポイントは X-Internal-Secret ヘッダーで保護（Cloud Scheduler想定）
- Virtual Threads 有効（spring.threads.virtual.enabled=true）
- Gemini API連携（要約・タグ付け）はフェーズ2スコープ外

## 環境変数
| 変数名 | 用途 |
|---|---|
| SUPABASE_URL | Supabase プロジェクトURL |
| SUPABASE_JWKS_URL | JWT公開鍵取得用JWKSエンドポイント |
| SUPABASE_DB_URL | PostgreSQL JDBC URL |
| DB_USERNAME | DBユーザー名 |
| DB_PASSWORD | DBパスワード |
| INTERNAL_BATCH_SECRET | バッチエンドポイント保護用シークレット |
| INTERNAL_BATCH_SECRET_REQUIRED | バッチ認証スキップフラグ（false で無効化、デフォルト true） |

## API一覧

### 記事API
| メソッド | パス | 認証 | 説明 |
|---|---|---|---|
| GET | `/api/articles` | JWT必須 | 記事一覧取得（ページネーション） |

クエリパラメータ：`page`（デフォルト0）、`size`（デフォルト20）

レスポンス（`ArticleResponse`）：`id`, `url`, `title`, `summary`, `tags`, `publishedAt`, `createdAt`, `bookmarked`（ログインユーザーが保存済みかどうか）

### ブックマークAPI
| メソッド | パス | 認証 | 説明 |
|---|---|---|---|
| POST | `/api/bookmarks/{articleId}` | JWT必須 | 記事を保存（201返却、重複時は冪等） |
| DELETE | `/api/bookmarks/{articleId}` | JWT必須 | 保存を解除（204返却） |
| GET | `/api/bookmarks` | JWT必須 | 保存済み記事一覧（ページネーション） |

クエリパラメータ：`page`（デフォルト0）、`size`（デフォルト20）

### バッチAPI
| メソッド | パス | 認証 | 説明 |
|---|---|---|---|
| POST | `/internal/batch/fetch` | X-Internal-Secret | RSSフィード全件取得・保存 |

### その他
| メソッド | パス | 認証 | 説明 |
|---|---|---|---|
| GET | `/actuator/health` | 不要 | ヘルスチェック |

## コーディング規約
- Lombokは使用しない（Java 21 Recordを活用）
- DTOはRecordで定義する
- コントローラーはthin（ロジックはServiceに集約）
- テストはフェーズ2以降で追加
