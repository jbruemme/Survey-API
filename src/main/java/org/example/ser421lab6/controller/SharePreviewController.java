package org.example.ser421lab6.controller;

import lombok.RequiredArgsConstructor;
import org.example.ser421lab6.entity.SurveyEntity;
import org.example.ser421lab6.repository.SurveyRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class SharePreviewController {

    private final SurveyRepository surveyRepository;

    @Value("${app.frontend-base-url}")
    private String frontendBaseUrl;

    @Value("${app.public-base-url-prod}")
    private String backendBaseUrl;

    /*
     * Public share preview endpoint for a survey.
     *
     * <p>This endpoint serves an HTML page containing Open Graph and Twitter Card
     * metadata for the specified survey, allowing social media platforms
     * (LinkedIn, Facebook, X) to generate preview cards when the link
     * is shared.</p>
     *
     * <p>After metadata is read, the page automatically redirects the user to the
     * frontend survey page.</p>
     *
     * <h3>Flow</h3>
     * <ol>
     *   <li>Social media crawler requests this endpoint</li>
     *   <li>HTML response contains metadata (title, image, description)</li>
     *   <li>Crawler generates preview card</li>
     *   <li>End user is redirected to the frontend survey page</li>
     * </ol>
     *
     * @param shareToken The unique public share token identifying the survey
     * @return HTML page with embedded metadata and redirect script
     *
     * @throws IllegalArgumentException if no survey exists for the provided share token
     */
    @GetMapping(value = "/s/{shareToken}", produces = MediaType.TEXT_HTML_VALUE)
    public String previewSurvey(@PathVariable String shareToken) {
        SurveyEntity survey = surveyRepository.findByShareToken(shareToken).orElse(null);

        String title = survey == null ? "Pulse Polling Survey" : escapeHtml(survey.getTitle());
        String frontendUrl = frontendBaseUrl + "/s/" + shareToken;
        String previewUrl = backendBaseUrl + "/s/" + shareToken;
        String imageUrl = frontendBaseUrl + "/pulse-preview.png";

        return """
                <!doctype html>
                <html>
                <head>
                    <meta charset="UTF-8" />
                    <title>%s</title>

                    <meta property="og:type" content="website" />
                    <meta property="og:title" content="%s" />
                    <meta property="og:description" content="Take this survey on Pulse Polling." />
                    <meta property="og:image" content="%s" />
                    <meta property="og:url" content="%s" />

                    <meta name="twitter:card" content="summary_large_image" />
                    <meta name="twitter:title" content="%s" />
                    <meta name="twitter:description" content="Take this survey on Pulse Polling." />
                    <meta name="twitter:image" content="%s" />

                    <meta http-equiv="refresh" content="0; url=%s" />
                </head>
                <body>
                    <p>Opening survey...</p>
                    <script>window.location.href = "%s";</script>
                </body>
                </html>
                """.formatted(
                title,
                title,
                imageUrl,
                previewUrl,
                title,
                imageUrl,
                frontendUrl,
                frontendUrl
        );
    }

    private String escapeHtml(String value) {
        if (value == null) return "";
        return value
                .replace("&", "&amp;")
                .replace("\"", "&quot;")
                .replace("<", "&lt;")
                .replace(">", "&gt;");
    }
}

