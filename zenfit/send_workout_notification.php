<?php
header('Content-Type: application/json');
include 'config.php';
require_once 'send_push_notification.php';

$response = array();

try {
    if (!isset($_POST['user_id']) || !isset($_POST['exercise_name']) || !isset($_POST['calories_burned'])) {
        throw new Exception('Required fields missing');
    }

    $userId = trim($_POST['user_id']);
    $exerciseName = trim($_POST['exercise_name']);
    $caloriesBurned = intval($_POST['calories_burned']);
    $sets = isset($_POST['sets']) ? intval($_POST['sets']) : 0;

    // Get user's FCM token
    $stmt = $conn->prepare("SELECT fcmToken, username FROM users WHERE id = ?");
    $stmt->bind_param("s", $userId);
    $stmt->execute();
    $result = $stmt->get_result();

    if ($result->num_rows > 0) {
        $user = $result->fetch_assoc();
        $fcmToken = $user['fcmToken'];
        $username = $user['username'];

        if (!empty($fcmToken)) {
            $title = "Workout Completed! 🎉";
            $body = "Great job, $username! You completed $sets sets of $exerciseName and burned $caloriesBurned calories!";
            
            $data = [
                "exercise_name" => $exerciseName,
                "calories_burned" => strval($caloriesBurned),
                "sets" => strval($sets)
            ];

            $notificationResult = sendFCMV1($fcmToken, $title, $body, $data);
            
            $response['statuscode'] = 200;
            $response['status'] = 'success';
            $response['message'] = 'Notification sent';
            $response['notification_result'] = json_decode($notificationResult);
        } else {
            $response['statuscode'] = 200;
            $response['status'] = 'success';
            $response['message'] = 'No FCM token available';
        }
    } else {
        throw new Exception('User not found');
    }

    $stmt->close();
} catch (Exception $e) {
    $response['statuscode'] = 500;
    $response['status'] = 'error';
    $response['message'] = $e->getMessage();
}

echo json_encode($response);
$conn->close();
?>
