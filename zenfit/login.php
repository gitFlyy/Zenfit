<?php
error_reporting(E_ALL);
ini_set('display_errors', 1);

include 'config.php';

$response = array();

if (isset($_POST['email'], $_POST['password'])) {
    $email = trim($_POST['email']);
    $password = $_POST['password'];

    if (empty($email) || empty($password)) {
        $response['statuscode'] = 400;
        $response['status'] = 'error';
        $response['message'] = 'Email and password are required';
        echo json_encode($response);
        exit();
    }

    $stmt = $conn->prepare("SELECT id, username, email, password, first_name, last_name, date_of_birth, location, city, profile_image, bio FROM users WHERE email = ?");
    
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

        if ($password === $user['password']) {
            unset($user['password']);

            $response['statuscode'] = 200;
            $response['status'] = 'success';
            $response['message'] = 'Login successful';
            $response['user'] = $user;
        } else {
            $response['statuscode'] = 401;
            $response['status'] = 'error';
            $response['message'] = 'Invalid password';
        }
    } else {
        $response['statuscode'] = 404;
        $response['status'] = 'error';
        $response['message'] = 'User not found';
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
