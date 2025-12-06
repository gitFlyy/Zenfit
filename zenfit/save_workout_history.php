<?php
header('Content-Type: application/json');
require_once 'config.php';

$user_id = $_POST['user_id'] ?? '';
$exercise_name = $_POST['exercise_name'] ?? '';
$reps = $_POST['reps'] ?? 0;
$sets = $_POST['sets'] ?? 0;
$weight = $_POST['weight'] ?? 0;
$duration = $_POST['duration'] ?? 0;
$rest_time = $_POST['rest_time'] ?? 0;
$calories_burned = $_POST['calories_burned'] ?? 0; // Add this line
$completed_date = $_POST['completed_date'] ?? time();

if (empty($user_id) || empty($exercise_name)) {
    echo json_encode(['success' => false, 'message' => 'Missing required fields']);
    exit;
}

// Updated SQL to include calories_burned
$stmt = $conn->prepare("INSERT INTO workout_history (user_id, exercise_name, reps, sets, weight, duration, rest_time, calories_burned, completed_date) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)");
$stmt->bind_param("ssiiiiiis", $user_id, $exercise_name, $reps, $sets, $weight, $duration, $rest_time, $calories_burned, $completed_date);

if ($stmt->execute()) {
    echo json_encode(['success' => true, 'message' => 'Workout saved to history']);
} else {
    echo json_encode(['success' => false, 'message' => 'Failed to save workout']);
}

$stmt->close();
$conn->close();
?>
