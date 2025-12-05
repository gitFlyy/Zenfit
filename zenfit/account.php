<?php
error_reporting(E_ALL);
ini_set('display_errors', 1);
header('Content-Type: application/json');

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

    $stmt = $conn->prepare("SELECT username, email, first_name, last_name, weight, height, profile_image FROM users WHERE id = ?");

    if (!$stmt) {
        $response['statuscode'] = 500;
        $response['status'] = 'error';
        $response['message'] = 'Database error: ' . $conn->error;
        echo json_encode($response);
        exit();
    }

    $stmt->bind_param("s", $user_id);
    $stmt->execute();
    $result = $stmt->get_result();

    if ($result->num_rows > 0) {
        $user = $result->fetch_assoc();
        $response['statuscode'] = 200;
        $response['status'] = 'success';
        $response['data'] = array(
            'username' => $user['username'] ?? '',
            'email' => $user['email'] ?? '',
            'first_name' => $user['first_name'] ?? '',
            'last_name' => $user['last_name'] ?? '',
            'weight' => $user['weight'] ?? '',
            'height' => $user['height'] ?? '',
            'profile_image' => $user['profile_image'] ?? null
        );
    } else {
        $response['statuscode'] = 404;
        $response['status'] = 'error';
        $response['message'] = 'User not found';
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
