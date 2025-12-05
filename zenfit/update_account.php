<?php
error_reporting(E_ALL);
ini_set('display_errors', 1);
header('Content-Type: application/json');

include 'config.php';

$response = array();

if (isset($_POST['user_id'], $_POST['username'], $_POST['email'], $_POST['first_name'], $_POST['last_name'], $_POST['weight'], $_POST['height'])) {
    $user_id = trim($_POST['user_id']);
    $username = trim($_POST['username']);
    $email = trim($_POST['email']);
    $first_name = trim($_POST['first_name']);
    $last_name = trim($_POST['last_name']);
    $weight = floatval($_POST['weight']);
    $height = trim($_POST['height']);
    $profile_image = isset($_POST['profile_image']) ? $_POST['profile_image'] : null;

    if (empty($user_id) || empty($username) || empty($email)) {
        $response['statuscode'] = 400;
        $response['status'] = 'error';
        $response['message'] = 'Required fields are missing';
        echo json_encode($response);
        exit();
    }

    // Update query with profile_image
    if ($profile_image !== null && !empty($profile_image)) {
        $stmt = $conn->prepare("UPDATE users SET username = ?, email = ?, first_name = ?, last_name = ?, weight = ?, height = ?, profile_image = ? WHERE id = ?");
        if (!$stmt) {
            $response['statuscode'] = 500;
            $response['status'] = 'error';
            $response['message'] = 'Database error: ' . $conn->error;
            echo json_encode($response);
            exit();
        }
        $stmt->bind_param("ssssdsss", $username, $email, $first_name, $last_name, $weight, $height, $profile_image, $user_id);
    } else {
        $stmt = $conn->prepare("UPDATE users SET username = ?, email = ?, first_name = ?, last_name = ?, weight = ?, height = ? WHERE id = ?");
        if (!$stmt) {
            $response['statuscode'] = 500;
            $response['status'] = 'error';
            $response['message'] = 'Database error: ' . $conn->error;
            echo json_encode($response);
            exit();
        }
        $stmt->bind_param("ssssdss", $username, $email, $first_name, $last_name, $weight, $height, $user_id);
    }

    if ($stmt->execute()) {
        $response['statuscode'] = 200;
        $response['status'] = 'success';
        $response['message'] = 'Account updated successfully';
    } else {
        $response['statuscode'] = 500;
        $response['status'] = 'error';
        $response['message'] = 'Failed to update account: ' . $stmt->error;
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
