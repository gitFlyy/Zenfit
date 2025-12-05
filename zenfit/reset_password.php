<?php
error_reporting(E_ALL);
ini_set('display_errors', 1);

date_default_timezone_set('Asia/Karachi');

include 'config.php';

$response = array();

if (isset($_POST['email'], $_POST['reset_code'], $_POST['new_password'])) {
    $email = trim($_POST['email']);
    $reset_code = trim($_POST['reset_code']);
    $new_password = $_POST['new_password'];

    if (empty($email) || empty($reset_code) || empty($new_password)) {
        $response['statuscode'] = 400;
        $response['status'] = 'error';
        $response['message'] = 'All fields are required';
        echo json_encode($response);
        exit();
    }

    $stmt = $conn->prepare("SELECT id FROM users WHERE email = ? AND reset_code = ? AND reset_code_expiry > NOW()");
    
    if (!$stmt) {
        $response['statuscode'] = 500;
        $response['status'] = 'error';
        $response['message'] = 'Database error: ' . $conn->error;
        echo json_encode($response);
        exit();
    }

    $stmt->bind_param("ss", $email, $reset_code);
    $stmt->execute();
    $result = $stmt->get_result();

    if ($result->num_rows > 0) {
        $user = $result->fetch_assoc();
        
        // Update password and clear reset code
        $update_stmt = $conn->prepare("UPDATE users SET password = ?, reset_code = NULL, reset_code_expiry = NULL WHERE id = ?");
        $update_stmt->bind_param("ss", $new_password, $user['id']);

        if ($update_stmt->execute()) {
            $response['statuscode'] = 200;
            $response['status'] = 'success';
            $response['message'] = 'Password reset successfully';
        } else {
            $response['statuscode'] = 500;
            $response['status'] = 'error';
            $response['message'] = 'Failed to reset password';
        }

        $update_stmt->close();
    } else {
        $response['statuscode'] = 401;
        $response['status'] = 'error';
        $response['message'] = 'Invalid or expired reset code';
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
