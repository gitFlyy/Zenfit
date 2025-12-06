<?php
header('Content-Type: application/json');
require_once 'config.php';

$ids = $_POST['ids'] ?? '';

if (empty($ids)) {
    echo json_encode(['success' => false, 'message' => 'No IDs provided']);
    exit;
}

$idsArray = explode(',', $ids);
$placeholders = implode(',', array_fill(0, count($idsArray), '?'));

$stmt = $conn->prepare("DELETE FROM workouts WHERE id IN ($placeholders)");
$stmt->bind_param(str_repeat('i', count($idsArray)), ...$idsArray);

if ($stmt->execute()) {
    echo json_encode(['success' => true, 'message' => 'Workouts deleted successfully']);
} else {
    echo json_encode(['success' => false, 'message' => 'Failed to delete workouts']);
}

$stmt->close();
$conn->close();
?>
