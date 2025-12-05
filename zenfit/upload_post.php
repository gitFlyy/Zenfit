<?php
header('Content-Type: application/json');

// Include your DB config
include 'config.php';

if ($_SERVER['REQUEST_METHOD'] !== 'POST') {
    echo json_encode(['success' => false, 'message' => 'Invalid request method']);
    exit;
}

$user_id = isset($_POST['user_id']) ? trim($_POST['user_id']) : '';
$image_base64 = isset($_POST['image_data']) ? $_POST['image_data'] : null;

if (empty($user_id) || empty($image_base64)) {
    echo json_encode(['success' => false, 'message' => 'Missing required fields (user_id or image_data)']);
    exit;
}

// Remove data URI prefix if present and validate base64
$image_data = $image_base64;
if (preg_match('/^data:image\/(png|jpg|jpeg|gif);base64,/', $image_base64)) {
    // Has data URI prefix, remove it
    $image_data = preg_replace('/^data:image\/(png|jpg|jpeg|gif);base64,/', '', $image_base64);
}

// Validate it's valid base64
if (!preg_match('/^[a-zA-Z0-9\/\r\n+]*={0,2}$/', trim($image_data))) {
    echo json_encode(['success' => false, 'message' => 'Invalid base64 format']);
    exit;
}

// Store only the base64 string without prefix
$sql = "INSERT INTO posts (user_id, image_data) VALUES (?, ?)";
$stmt = $conn->prepare($sql);
if (!$stmt) {
    echo json_encode(['success' => false, 'message' => 'DB prepare failed: ' . $conn->error]);
    exit;
}

$stmt->bind_param('ss', $user_id, $image_data);

if ($stmt->execute()) {
    echo json_encode([
        'success' => true,
        'message' => 'Post uploaded successfully',
        'post_id' => $conn->insert_id
    ]);
} else {
    echo json_encode(['success' => false, 'message' => 'Insert failed: ' . $stmt->error]);
}

$stmt->close();
$conn->close();
?>
