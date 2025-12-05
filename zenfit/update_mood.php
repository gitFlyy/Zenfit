<?php
header('Content-Type: application/json');

// Database connection
include 'config.php';

$response = array();

if ($_SERVER['REQUEST_METHOD'] == 'POST') {
    $user_id = $_POST['user_id'];
    $mood = $_POST['mood'];
    $sleep_hours = isset($_POST['sleep_hours']) ? $_POST['sleep_hours'] : null;
    $sleep_quality = isset($_POST['sleep_quality']) ? $_POST['sleep_quality'] : null;

    if (!empty($user_id) && !empty($mood)) {
        $stmt = $conn->prepare("INSERT INTO mood (user_id, mood, sleep_hours, sleep_quality, created_at) VALUES (?, ?, ?, ?, NOW()) ON DUPLICATE KEY UPDATE mood = ?, sleep_hours = ?, sleep_quality = ?, created_at = NOW()");
        $stmt->bind_param("sssssss", $user_id, $mood, $sleep_hours, $sleep_quality, $mood, $sleep_hours, $sleep_quality);

        if ($stmt->execute()) {
            $response['success'] = true;
            $response['message'] = "Mood updated successfully.";
        } else {
            $response['success'] = false;
            $response['message'] = "Failed to update mood.";
        }

        $stmt->close();
    } else {
        $response['success'] = false;
        $response['message'] = "Invalid input.";
    }
} else {
    $response['success'] = false;
    $response['message'] = "Invalid request method.";
}

$conn->close();
echo json_encode($response);
?>
