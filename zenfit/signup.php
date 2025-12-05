<?php
error_reporting(E_ALL);
ini_set('display_errors', 1);
header('Content-Type: application/json');

include 'config.php';

$response = array();

if (isset($_POST['username'], $_POST['email'], $_POST['password'], $_POST['first_name'], $_POST['last_name'], $_POST['date_of_birth'])) {
    $username = trim($_POST['username']);
    $email = trim($_POST['email']);
    $password = $_POST['password'];
    $first_name = trim($_POST['first_name']);
    $last_name = trim($_POST['last_name']);
    $date_of_birth = $_POST['date_of_birth'];
    $location = isset($_POST['location']) ? trim($_POST['location']) : '';
    $city = isset($_POST['city']) ? trim($_POST['city']) : '';
    $profile_image = isset($_POST['profile_image']) ? $_POST['profile_image'] : null;

    if (empty($username) || empty($email) || empty($password) || empty($first_name) || empty($last_name) || empty($date_of_birth)) {
        $response['statuscode'] = 400;
        $response['status'] = 'error';
        $response['message'] = 'All required fields must be filled';
        echo json_encode($response);
        exit();
    }

    $check_stmt = $conn->prepare("SELECT id FROM users WHERE email = ? OR username = ?");
    $check_stmt->bind_param("ss", $email, $username);
    $check_stmt->execute();
    $check_result = $check_stmt->get_result();

    if ($check_result->num_rows > 0) {
        $response['statuscode'] = 409;
        $response['status'] = 'error';
        $response['message'] = 'User with this email or username already exists';
        echo json_encode($response);
        exit();
    }

    $userId = uniqid('user_', true);

    $stmt = $conn->prepare("INSERT INTO users (id, username, email, password, first_name, last_name, date_of_birth, location, city, profile_image, created_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, NOW())");

    if (!$stmt) {
        $response['statuscode'] = 500;
        $response['status'] = 'error';
        $response['message'] = 'Database error: ' . $conn->error;
        echo json_encode($response);
        exit();
    }

    $stmt->bind_param("ssssssssss", $userId, $username, $email, $password, $first_name, $last_name, $date_of_birth, $location, $city, $profile_image);

    if ($stmt->execute()) {
        $response['statuscode'] = 200;
        $response['status'] = 'success';
        $response['message'] = 'Registration successful';
        $response['user'] = array(
            'id' => $userId,
            'username' => $username,
            'email' => $email,
            'first_name' => $first_name,
            'last_name' => $last_name
        );
    } else {
        $response['statuscode'] = 500;
        $response['status'] = 'error';
        $response['message'] = 'Registration failed: ' . $stmt->error;
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
