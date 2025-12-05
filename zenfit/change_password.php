<?php
error_reporting(E_ALL);
ini_set('display_errors', 1);

include 'config.php';

$response = array();

if (isset($_POST['user_id']) && isset($_POST['old_password']) && isset($_POST['new_password'])) {
    $user_id = trim($_POST['user_id']);
    $old_password = trim($_POST['old_password']);
    $new_password = trim($_POST['new_password']);

    if (empty($user_id) || empty($old_password) || empty($new_password)) {
        $response['statuscode'] = 400;
        $response['status'] = 'error';
        $response['message'] = 'All fields are required';
        echo json_encode($response);
        exit();
    }

    // Get current password from database
    $stmt = $conn->prepare("SELECT password FROM users WHERE id = ?");
    $stmt->bind_param("s", $user_id);
    $stmt->execute();
    $result = $stmt->get_result();

    if ($result->num_rows > 0) {
        $user = $result->fetch_assoc();
        
        // Verify old password matches
        if ($user['password'] !== $old_password) {
            $response['statuscode'] = 401;
            $response['status'] = 'error';
            $response['message'] = 'Current password is incorrect';
            echo json_encode($response);
            $stmt->close();
            exit();
        }

        // Update to new password
        $update_stmt = $conn->prepare("UPDATE users SET password = ? WHERE id = ?");
        $update_stmt->bind_param("ss", $new_password, $user_id);

        if ($update_stmt->execute()) {
            $response['statuscode'] = 200;
            $response['status'] = 'success';
            $response['message'] = 'Password updated successfully';
        } else {
            $response['statuscode'] = 500;
            $response['status'] = 'error';
            $response['message'] = 'Failed to update password';
        }

        $update_stmt->close();
    } else {
        $response['statuscode'] = 404;
        $response['status'] = 'error';
        $response['message'] = 'User not found';
    }

    $stmt->close();
} else {
    $response['statuscode'] = 400;
    $response['status'] = 'error';
    $response['message'] = 'Missing required parameters';
}

echo json_encode($response);
$conn->close();
?>
