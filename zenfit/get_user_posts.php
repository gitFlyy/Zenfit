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

// Fetch user posts ordered by most recent first
$sql = "SELECT id, user_id, image_data, created_at 
        FROM posts 
        WHERE user_id = ? 
        ORDER BY created_at DESC";

$stmt = $conn->prepare($sql);
$stmt->bind_param("s", $user_id);
$stmt->execute();
$result = $stmt->get_result();

$posts = array();

if ($result->num_rows > 0) {
    while ($row = $result->fetch_assoc()) {
        $posts[] = array(
            "id" => $row['id'],
            "user_id" => $row['user_id'],
            "image_data" => $row['image_data'],
            "created_at" => $row['created_at']
        );
    }
}

// Return posts array (empty if no posts found)
echo json_encode(array(
    "statuscode" => 200,
    "status" => "success",
    "message" => count($posts) > 0 ? "Posts fetched successfully" : "No posts found",
    "data" => $posts
));

$stmt->close();
$conn->close();
?>
