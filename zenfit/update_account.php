<?php
error_reporting(E_ALL);
ini_set('display_errors', 1);
header('Content-Type: application/json');

include 'config.php';

$response = array();

if (isset($_POST['user_id'], $_POST['weight'], $_POST['height'], $_POST['daily_activity_minutes'])) {
    $user_id = trim($_POST['user_id']);
    $weight = floatval($_POST['weight']);
    $height = trim($_POST['height']);
    $activity = intval($_POST['daily_activity_minutes']);

    if (empty($user_id)) {
        $response['statuscode'] = 400;
        $response['status'] = 'error';
        $response['message'] = 'User ID is required';
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

    $stmt->bind_param("dsis", $weight, $height, $activity, $user_id);

    if ($stmt->execute()) {
        $response['statuscode'] = 200;
        $response['status'] = 'success';
        $response['message'] = 'Setup completed successfully';
    } else {
        $response['statuscode'] = 500;
        $response['status'] = 'error';
        $response['message'] = 'Failed to update: ' . $stmt->error;
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
