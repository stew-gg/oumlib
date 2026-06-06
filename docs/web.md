# Web & Discord Webhooks

OumLib includes a built-in, lightweight, asynchronous Discord Webhook client that relies on Java's standard `HttpClient` and a custom, zero-reflection JSON builder. This allows plugins to log matches, alerts, and announcements directly to Discord without causing server tick lag.

---

## 1. Asynchronous Discord Webhook Client

The Webhook builder makes it simple to construct and dispatch standard Discord webhooks. Since the network requests are executed asynchronously on a background thread pool, they are safe to use on both Paper (including Folia) and Velocity.

### Simple Text Message
To send a plain text message:

```java
import dev.oum.oumlib.web.Webhook;

Webhook.url("https://discord.com/api/webhooks/...")
    .username("Server Logger")
    .avatarUrl("https://my-icon-url.png")
    .content("The server has successfully started up!")
    .sendAsync()
    .whenComplete((result, exception) -> {
        if (exception != null) {
            System.err.println("Failed to send webhook: " + exception.getMessage());
        }
    });
```

---

## 2. Rich Discord Embeds

For structured logs, you can build rich Discord Embeds featuring custom titles, colors, fields, footers, authors, and images.

### Example: Match Logger Embed

```java
import dev.oum.oumlib.web.Webhook;
import dev.oum.oumlib.web.WebhookEmbed;

Webhook.url("https://discord.com/api/webhooks/...")
    .username("CoinFlip Logger")
    .avatarUrl("https://my-avatar-url.png")
    .embed(embed -> embed
        .title("CoinFlip Match Concluded")
        .description("maceful won **$100.00** against sun_mc in a coinflip!")
        .color(0xFFAA00) // Hex or decimal color code
        .thumbnail("https://my-coin-image.png")
        .author("CoinFlip Logs", null, null)
        .field("Winner", "maceful", true)
        .field("Loser", "sun_mc", true)
        .field("Wager", "$50.00", true)
        .footer("CoinFlip • Match Logs", null)
    )
    .sendAsync()
    .whenComplete(null, ex -> {
        System.err.println("Failed to send log to Discord: " + ex.getMessage());
    });
```

### Advanced: Custom WebhookEmbed Objects
If you prefer to separate the construction of the embed from the webhook dispatcher, you can build a reusable `WebhookEmbed` object:

```java
import dev.oum.oumlib.web.WebhookEmbed;

WebhookEmbed matchEmbed = WebhookEmbed.builder()
    .title("Leaderboard Update")
    .description("A new player has entered the top leaderboard!")
    .color(0x55FF55)
    .field("Player", "maceful", false)
    .field("Wins", "150", true)
    .field("Win Rate", "74.5%", true)
    .footer("Leaderboards Tracker", null)
    .build();

Webhook.url("https://discord.com/api/webhooks/...")
    .username("Leaderboards")
    .embed(matchEmbed)
    .sendAsync();
```
