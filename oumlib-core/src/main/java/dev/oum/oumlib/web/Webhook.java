package dev.oum.oumlib.web;

import dev.oum.oumlib.scheduler.Promise;
import org.jetbrains.annotations.Contract;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public final class Webhook {

    private static final HttpClient HTTP_CLIENT = HttpClient.newHttpClient();

    private final String url;
    private final String username;
    private final String avatarUrl;
    private final String content;
    private final List<WebhookEmbed> embeds;

    @Contract(pure = true)
    private Webhook(@NonNull Builder builder) {
        this.url = builder.url;
        this.username = builder.username;
        this.avatarUrl = builder.avatarUrl;
        this.content = builder.content;
        this.embeds = List.copyOf(builder.embeds);
    }

    public static @NonNull Builder url(@NonNull String url) {
        return new Builder(url);
    }

    public @NonNull Promise<Void> sendAsync() {
        return Promise.supplyAsync(() -> {
            try {
                String json = toJson();
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(url))
                        .header("Content-Type", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofString(json))
                        .build();

                HttpResponse<String> response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
                if (response.statusCode() < 200 || response.statusCode() >= 300) {
                    throw new RuntimeException("Webhook request failed with status: " + response.statusCode() + " - " + response.body());
                }
            } catch (Exception e) {
                throw new RuntimeException("Failed to send webhook request", e);
            }
            return null;
        });
    }

    private @NonNull String toJson() {
        StringBuilder sb = new StringBuilder();
        sb.append("{");

        boolean first = true;
        if (username != null) {
            sb.append("\"username\":\"").append(escapeJson(username)).append("\"");
            first = false;
        }
        if (avatarUrl != null) {
            if (!first) sb.append(",");
            sb.append("\"avatar_url\":\"").append(escapeJson(avatarUrl)).append("\"");
            first = false;
        }
        if (content != null) {
            if (!first) sb.append(",");
            sb.append("\"content\":\"").append(escapeJson(content)).append("\"");
            first = false;
        }

        if (!embeds.isEmpty()) {
            if (!first) sb.append(",");
            sb.append("\"embeds\":[");
            for (int i = 0; i < embeds.size(); i++) {
                if (i > 0) sb.append(",");
                appendEmbedJson(sb, embeds.get(i));
            }
            sb.append("]");
        }

        sb.append("}");
        return sb.toString();
    }

    private void appendEmbedJson(@NonNull StringBuilder sb, @NonNull WebhookEmbed embed) {
        sb.append("{");
        boolean first = true;

        if (embed.title() != null) {
            sb.append("\"title\":\"").append(escapeJson(embed.title())).append("\"");
            first = false;
        }
        if (embed.description() != null) {
            if (!first) sb.append(",");
            sb.append("\"description\":\"").append(escapeJson(embed.description())).append("\"");
            first = false;
        }
        if (embed.color() != null) {
            if (!first) sb.append(",");
            sb.append("\"color\":").append(embed.color());
            first = false;
        }

        if (embed.thumbnailUrl() != null) {
            if (!first) sb.append(",");
            sb.append("\"thumbnail\":{\"url\":\"").append(escapeJson(embed.thumbnailUrl())).append("\"}");
            first = false;
        }

        if (embed.footerText() != null) {
            if (!first) sb.append(",");
            sb.append("\"footer\":{");
            sb.append("\"text\":\"").append(escapeJson(embed.footerText())).append("\"");
            if (embed.footerIcon() != null) {
                sb.append(",\"icon_url\":\"").append(escapeJson(embed.footerIcon())).append("\"");
            }
            sb.append("}");
            first = false;
        }

        if (embed.authorName() != null) {
            if (!first) sb.append(",");
            sb.append("\"author\":{");
            sb.append("\"name\":\"").append(escapeJson(embed.authorName())).append("\"");
            if (embed.authorUrl() != null) {
                sb.append(",\"url\":\"").append(escapeJson(embed.authorUrl())).append("\"");
            }
            if (embed.authorIcon() != null) {
                sb.append(",\"icon_url\":\"").append(escapeJson(embed.authorIcon())).append("\"");
            }
            sb.append("}");
            first = false;
        }

        List<WebhookEmbedField> fields = embed.fields();
        if (!fields.isEmpty()) {
            if (!first) sb.append(",");
            sb.append("\"fields\":[");
            for (int i = 0; i < fields.size(); i++) {
                if (i > 0) sb.append(",");
                WebhookEmbedField f = fields.get(i);
                sb.append("{");
                sb.append("\"name\":\"").append(escapeJson(f.name())).append("\",");
                sb.append("\"value\":\"").append(escapeJson(f.value())).append("\",");
                sb.append("\"inline\":").append(f.inline());
                sb.append("}");
            }
            sb.append("]");
        }

        sb.append("}");
    }

    private static @NonNull String escapeJson(String value) {
        if (value == null) return "";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < value.length(); i++) {
            char ch = value.charAt(i);
            switch (ch) {
                case '"' -> sb.append("\\\"");
                case '\\' -> sb.append("\\\\");
                case '\b' -> sb.append("\\b");
                case '\f' -> sb.append("\\f");
                case '\n' -> sb.append("\\n");
                case '\r' -> sb.append("\\r");
                case '\t' -> sb.append("\\t");
                default -> {
                    if (ch < ' ') {
                        String t = "000" + Integer.toHexString(ch);
                        sb.append("\\u").append(t.substring(t.length() - 4));
                    } else {
                        sb.append(ch);
                    }
                }
            }
        }
        return sb.toString();
    }

    public static final class Builder {
        private final String url;
        private String username;
        private String avatarUrl;
        private String content;
        private final List<WebhookEmbed> embeds = new ArrayList<>();

        private Builder(String url) {
            this.url = url;
        }

        public @NonNull Builder username(@Nullable String username) {
            this.username = username;
            return this;
        }

        public @NonNull Builder avatarUrl(@Nullable String avatarUrl) {
            this.avatarUrl = avatarUrl;
            return this;
        }

        public @NonNull Builder content(@Nullable String content) {
            this.content = content;
            return this;
        }

        public @NonNull Builder embed(@NonNull WebhookEmbed embed) {
            this.embeds.add(embed);
            return this;
        }

        public @NonNull Builder embed(@NonNull Consumer<WebhookEmbed.Builder> builderConsumer) {
            WebhookEmbed.Builder builder = WebhookEmbed.builder();
            builderConsumer.accept(builder);
            this.embeds.add(builder.build());
            return this;
        }

        public @NonNull Promise<Void> sendAsync() {
            return new Webhook(this).sendAsync();
        }
    }
}
