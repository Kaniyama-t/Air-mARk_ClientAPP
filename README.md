
# Air mARk for Android

[![Build Status](https://app.bitrise.io/app/a7e4189193766625/status.svg?token=00-NlvZ8mJ7RsrMZHgWKLA&branch=master)](https://app.bitrise.io/app/a7e4189193766625)[![Platform](https://img.shields.io/badge/platform-android-green)]()[![Event](https://img.shields.io/badge/event-HackU2019%20Osaka-orange)](https://hacku.yahoo.co.jp/hacku2019osaka/)
[![deploy](https://img.shields.io/badge/debug-deploygate-gray)](https://deploygate.com/login)[![issue](https://img.shields.io/badge/issues-Github-lightgrey)](https://github.com/Kaniyama-t/Air-mARk_ClientAPP/issues)[![Scheduire](https://img.shields.io/badge/schedule-wrike-brightgreen)]()

## OverView

本システム専用の二次元コードをAR表示するアプリ



## Demo (ScreenShots)

まだです！<(_ _;)>



## Usage (debug)

1. [deploygate](https://deploygate.com/login)のアカウントを用意
2. デバッグ用の端末に[deploygateアプリ](https://play.google.com/store/apps/details?id=com.deploygate&hl=ja)を導入し、ログインを済ませる
3. チームSlackの"Kaniyama_t"にdeploygateのユーザ名を送る
4. 蟹から返信が来たらdeploygateアプリを開いてください
   Air mARkがインストールできるはずです。

**※免責事項：deploygateのコンソール経由で端末機種名が蟹にバレます...(仕様)**



## For contribute

　本システムはオレオレClean ArchitectureやRxJava,DIcontainerを採用しています．くれぐれもコード閲覧時はご注意ください．

#### 〇 issue (バグ報告／仕様との相違点)

　[こちら](https://github.com/Kaniyama-t/Air-mARk_ClientAPP/issues/new)から，タグ"！Bug／Improve"を指定し，AssignmentにKaniyama_tを含めて投稿してください．
　ボディに画面のSSや説明を載せて頂けると有難いです．
　Bodyが詳細に書けない場合は，Slackに連絡をお願いします．

#### 〇 issue (機能追加)

　[こちら](https://github.com/Kaniyama-t/Air-mARk_ClientAPP/issues/new)から，タグ"＋Add"を指定し，AssignmentにKaniyama_tを含めて投稿してください．
　よほど確定出ない限りは，事前にSlackで相談いただけると幸いです．

#### 〇 Pull Request／fork

　~~私自身このあたりの機能よくわかっていません．~~ (おい)
　Commit時は**必ず"develop"ブランチをforkして作業**してください．ブランチ名は"自身の名前-作業内容"という形式を推奨．
　作業前やmasterへのマージ前に必ず相談ください(コンフリクト防止のため)．
　クラスの依存関係は多分Wikiに掲載していると思われます．不明な点はSlackへ．



## Flow

#### trigger:masterブランチにcommitが追加された

1. bitriseがUnitTest，そのままapkへビルドしSign
2. deploygateに自動アップロードされる
3. Slackへ通知が飛ぶ

#### trigger:issue追加時

1. wrikeにタスクとして追加される．