<?php
error_reporting(E_ALL);
ini_set('display_errors', 1);

include 'config.php';

$response = array();

if (isset($_POST['user_id'], $_POST['weight'], $_POST['height'], $_POST['daily_activity_minutes'])) {
    $user_id = trim($_POST['user_id']);
    $weight = floatval($_POST['weight']);
    $height = floatval($_POST['height']);
    $daily_activity_minutes = intval($_POST['daily_activity_minutes']);

    if (empty($user_id) || $weight <= 0 || $height <= 0 || $daily_activity_minutes < 0) {
        $response['statuscode'] = 400;
        $response['status'] = 'error';
        $response['message'] = 'Invalid input data';
        echo json_encode($response);
        exit();
    }

    $stmt = $conn->prepare("UPDATE users SET weight = ?, height = ?, daily_activity_minutes = ? WHERE id = ?");

    if (!$stmt) {
        $response['statuscode'] = 500;
        $response['status'] = 'error';
        $response['message'] = 'Database error: ' . $conn->error;
        echo json_encode($response);
        exit();
    }

    $stmt->bind_param("ddis", $weight, $height, $daily_activity_minutes, $user_id);

    if ($stmt->execute()) {
        $response['statuscode'] = 200;
        $response['status'] = 'success';
        $response['message'] = 'Profile updated successfully';
    } else {
        $response['statuscode'] = 500;
        $response['status'] = 'error';
        $response['message'] = 'Failed to update profile: ' . $stmt->error;
    }

    $stmt->close();
} else {
    $response['statuscode'] = 400;
    $response['status'] = 'error';
    $response['message'] = 'Required fields are missing';
}

echo json_encode($response);
$conn->close();
?>
