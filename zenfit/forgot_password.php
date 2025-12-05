<?php
error_reporting(E_ALL);
ini_set('display_errors', 1);

// Set timezone to match your location (Pakistan)
date_default_timezone_set('Asia/Karachi');

include 'config.php';

$response = array();

if (isset($_POST['email'])) {
    $email = trim($_POST['email']);

    if (empty($email)) {
        $response['statuscode'] = 400;
        $response['status'] = 'error';
        $response['message'] = 'Email field is missing';
        echo json_encode($response);
        exit();
    }

    $stmt = $conn->prepare("SELECT id FROM users WHERE email = ?");

    if (!$stmt) {
        $response['statuscode'] = 500;
        $response['status'] = 'error';
        $response['message'] = 'Database error: ' . $conn->error;
        echo json_encode($response);
        exit();
    }

    $stmt->bind_param("s", $email);
    $stmt->execute();
    $result = $stmt->get_result();

    if ($result->num_rows > 0) {
        $user = $result->fetch_assoc();

        $reset_code = str_pad(rand(0, 999999), 6, '0', STR_PAD_LEFT);
        $expiry_time = date('Y-m-d H:i:s', strtotime('+15 minutes'));

        $update_stmt = $conn->prepare("UPDATE users SET reset_code = ?, reset_code_expiry = ? WHERE id = ?");
        $update_stmt->bind_param("sss", $reset_code, $expiry_time, $user['id']);

        if ($update_stmt->execute()) {
            $response['statuscode'] = 200;
            $response['status'] = 'success';
            $response['message'] = 'Reset code sent successfully';
            $response['reset_code'] = $reset_code;
            $response['user_id'] = $user['id'];
        } else {
            $response['statuscode'] = 500;
            $response['status'] = 'error';
            $response['message'] = 'Failed to generate reset code';
        }

        $update_stmt->close();
    } else {
        $response['statuscode'] = 404;
        $response['status'] = 'error';
        $response['message'] = 'No account found with this email';
    }

    $stmt->close();
} else {
    $response['statuscode'] = 400;
    $response['status'] = 'error';
    $response['message'] = 'Email field is missing';
}

echo json_encode($response);
$conn->close();
?>
