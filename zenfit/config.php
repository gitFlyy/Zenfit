<?php
$servername = "localhost";
$username = "root";
$password = "";
$dbname = "zenfit_db";

$conn = new mysqli($servername, $username, $password, $dbname);

if ($conn->connect_error) {
    die(json_encode(array(
        "statuscode" => 500,
        "status" => "error",
        "message" => "Connection failed: " . $conn->connect_error
    )));
}
?>
