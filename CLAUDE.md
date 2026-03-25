# SDV News Backend - Claude Code ルール

## プロジェクト概要
SDV（Software Defined Vehicle）関連ニュースを収集・配信するSpring Bootバックエンド。

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
└── batch/      RSSバッチ処理
```

## 設計方針
- ステートレス認証（Supabase HS256 JWT）
- バッチエンドポイントは X-Internal-Secret ヘッダーで保護（Cloud Scheduler想定）
- Virtual Threads 有効（spring.threads.virtual.enabled=true）
- Gemini API連携（要約・タグ付け）はフェーズ2スコープ外

## 環境変数
| 変数名 | 用途 |
|---|---|
| SUPABASE_URL | Supabase プロジェクトURL |
| SUPABASE_JWT_SECRET | JWT署名検証シークレット |
| SUPABASE_DB_URL | PostgreSQL JDBC URL |
| DB_USERNAME | DBユーザー名 |
| DB_PASSWORD | DBパスワード |
| INTERNAL_BATCH_SECRET | バッチエンドポイント保護用シークレット |

## コーディング規約
- Lombokは使用しない（Java 21 Recordを活用）
- DTOはRecordで定義する
- コントローラーはthin（ロジックはServiceに集約）
- テストはフェーズ2以降で追加
