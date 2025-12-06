<?php
header('Content-Type: application/json');
require_once 'config.php';

$user_id = $_POST['user_id'] ?? '';

if (empty($user_id)) {
    echo json_encode(['success' => false, 'message' => 'User ID required']);
    exit;
}

$stmt = $conn->prepare("SELECT * FROM workout_history WHERE user_id = ? ORDER BY completed_date DESC");
$stmt->bind_param("s", $user_id);
$stmt->execute();
$result = $stmt->get_result();

$workouts = [];
while ($row = $result->fetch_assoc()) {
    $workouts[] = [
        'id' => $row['id'],
        'exercise_name' => $row['exercise_name'],
        'reps' => $row['reps'],
        'sets' => $row['sets'],
        'weight' => $row['weight'],
        'duration' => $row['duration'],
        'rest_time' => $row['rest_time'],
        'completed_date' => $row['completed_date']
    ];
}

echo json_encode(['success' => true, 'workouts' => $workouts]);

$stmt->close();
$conn->close();
?>
