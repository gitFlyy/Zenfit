<?php
require_once 'config.php';

header('Content-Type: application/json');

if ($_SERVER['REQUEST_METHOD'] !== 'POST') {
    echo json_encode(array(
        "statuscode" => 405,
        "status" => "error",
        "message" => "Method not allowed"
    ));
    exit();
}

// Get user_id from POST request
$user_id = isset($_POST['user_id']) ? trim($_POST['user_id']) : '';

if (empty($user_id)) {
    echo json_encode(array(
        "statuscode" => 400,
        "status" => "error",
        "message" => "User ID is required"
    ));
    exit();
}

// Fetch user profile data
$sql = "SELECT id, username, email, first_name, last_name, date_of_birth, location, city, 
        weight, height, daily_activity_minutes, profile_image, bio, created_at 
        FROM users WHERE id = ?";

$stmt = $conn->prepare($sql);
$stmt->bind_param("s", $user_id);
$stmt->execute();
$result = $stmt->get_result();

if ($result->num_rows > 0) {
    $user = $result->fetch_assoc();
    
    // Get post count for this user
    $count_sql = "SELECT COUNT(*) as post_count FROM posts WHERE user_id = ?";
    $count_stmt = $conn->prepare($count_sql);
    $count_stmt->bind_param("s", $user_id);
    $count_stmt->execute();
    $count_result = $count_stmt->get_result();
    $count_data = $count_result->fetch_assoc();
    
    // Prepare response
    $response = array(
        "statuscode" => 200,
        "status" => "success",
        "message" => "Profile fetched successfully",
        "data" => array(
            "id" => $user['id'],
            "username" => $user['username'],
            "email" => $user['email'],
            "first_name" => $user['first_name'],
            "last_name" => $user['last_name'],
            "full_name" => trim(($user['first_name'] ?? '') . ' ' . ($user['last_name'] ?? '')),
            "date_of_birth" => $user['date_of_birth'],
            "location" => $user['location'],
            "city" => $user['city'],
            "weight" => $user['weight'],
            "height" => $user['height'],
            "daily_activity_minutes" => $user['daily_activity_minutes'],
            "profile_image" => $user['profile_image'],
            "bio" => $user['bio'],
            "created_at" => $user['created_at'],
            "post_count" => $count_data['post_count']
        )
    );
    
    echo json_encode($response);
    $count_stmt->close();
} else {
    echo json_encode(array(
        "statuscode" => 404,
        "status" => "error",
        "message" => "User not found"
    ));
}

$stmt->close();
$conn->close();
?>
