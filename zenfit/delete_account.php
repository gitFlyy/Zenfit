<?php
error_reporting(E_ALL);
ini_set('display_errors', 1);

include 'config.php';

$response = array();

if (isset($_POST['user_id'])) {
    $user_id = trim($_POST['user_id']);

    if (empty($user_id)) {
        $response['statuscode'] = 400;
        $response['status'] = 'error';
        $response['message'] = 'User ID is required';
        echo json_encode($response);
        exit();
    }

    $stmt = $conn->prepare("DELETE FROM users WHERE id = ?");

    if (!$stmt) {
        $response['statuscode'] = 500;
        $response['status'] = 'error';
        $response['message'] = 'Database error: ' . $conn->error;
        echo json_encode($response);
        exit();
    }

    $stmt->bind_param("s", $user_id);

    if ($stmt->execute()) {
        $response['statuscode'] = 200;
        $response['status'] = 'success';
        $response['message'] = 'Account deleted successfully';
    } else {
        $response['statuscode'] = 500;
        $response['status'] = 'error';
        $response['message'] = 'Failed to delete account: ' . $stmt->error;
    }

    $stmt->close();
} else {
    $response['statuscode'] = 400;
    $response['status'] = 'error';
    $response['message'] = 'User ID is missing';
}

echo json_encode($response);
$conn->close();
?>
