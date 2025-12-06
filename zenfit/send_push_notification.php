<?php

function sendFCMV1($deviceToken, $title, $body, $data = []) {

    // Path to your service account file
    $serviceAccountPath = "C:/xampp/htdocs/sociallysmd-firebase-adminsdk-fbsvc-9a42dbc783.json";

    // Project ID (VERY IMPORTANT)
    $projectId = "sociallysmd"; // <-- Change this to YOUR project ID from Firebase

    // Create access token
    $jwt = createAccessToken($serviceAccountPath);

    // Message payload
    $message = [
        "message" => [
            "token" => $deviceToken,
            "notification" => [
                "title" => $title,
                "body" => $body
            ],
            "data" => $data
        ]
    ];

    $url = "https://fcm.googleapis.com/v1/projects/$projectId/messages:send";

    $headers = [
        "Authorization: Bearer $jwt",
        "Content-Type: application/json; charset=UTF-8"
    ];

    $ch = curl_init($url);
    curl_setopt($ch, CURLOPT_POST, true);
    curl_setopt($ch, CURLOPT_HTTPHEADER, $headers);
    curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);
    curl_setopt($ch, CURLOPT_POSTFIELDS, json_encode($message));

    $result = curl_exec($ch);
    curl_close($ch);

    return $result;
}



// ---------------------------------------------
// Create OAuth2 Access Token
// ---------------------------------------------
function createAccessToken($serviceAccountPath) {

    $json = json_decode(file_get_contents($serviceAccountPath), true);

    $privateKey = $json['private_key'];
    $clientEmail = $json['client_email'];

    $now = time();
    $expires = $now + 3600; // 1 hour

    $header = base64_encode(json_encode([
        "alg" => "RS256",
        "typ" => "JWT"
    ]));

    $claimSet = base64_encode(json_encode([
        "iss" => $clientEmail,
        "scope" => "https://www.googleapis.com/auth/firebase.messaging",
        "aud" => "https://oauth2.googleapis.com/token",
        "iat" => $now,
        "exp" => $expires
    ]));

    $signatureInput = $header . "." . $claimSet;

    openssl_sign($signatureInput, $signature, $privateKey, "SHA256");

    $jwt = $signatureInput . "." . base64_encode($signature);

    // Exchange JWT for access token
    $postData = http_build_query([
        "grant_type" => "urn:ietf:params:oauth:grant-type:jwt-bearer",
        "assertion" => $jwt
    ]);

    $ch = curl_init("https://oauth2.googleapis.com/token");
    curl_setopt($ch, CURLOPT_POST, true);
    curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);
    curl_setopt($ch, CURLOPT_POSTFIELDS, $postData);

    $response = curl_exec($ch);
    curl_close($ch);

    $tokenData = json_decode($response, true);

    return $tokenData["access_token"];
}

?>
