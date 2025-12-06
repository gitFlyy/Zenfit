<?php
header('Content-Type: application/json');
include 'config.php';

$response = array();

try {
    if (!isset($_POST['userId']) || !isset($_POST['fcmToken'])) {
        throw new Exception('Required fields missing');
    }

    $userId = trim($_POST['userId']);
    $fcmToken = trim($_POST['fcmToken']);

    // First, check if fcmToken column exists, if not add it
    $checkColumn = $conn->query("SHOW COLUMNS FROM users LIKE 'fcmToken'");
    if ($checkColumn->num_rows == 0) {
        $conn->query("ALTER TABLE users ADD COLUMN fcmToken VARCHAR(255) DEFAULT NULL");
    }

    // Update the user's FCM token
    $stmt = $conn->prepare("UPDATE users SET fcmToken = ? WHERE id = ?");
    $stmt->bind_param("ss", $fcmToken, $userId);

    if ($stmt->execute()) {
        $response['statuscode'] = 200;
        $response['status'] = 'success';
        $response['message'] = 'FCM token updated successfully';
    } else {
        throw new Exception('Failed to update FCM token');
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
