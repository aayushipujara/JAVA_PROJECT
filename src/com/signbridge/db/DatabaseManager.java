package com.signbridge.db;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseManager {

    public static void initializeDatabase() {
        Connection conn = DBConnection.getConnection();
        if (conn == null) {
            System.err.println("[DatabaseManager] Database connection is null. Cannot initialize schema.");
            return;
        }

        try (Statement stmt = conn.createStatement()) {
            boolean isSQLite = "sqlite".equalsIgnoreCase(DBConnection.getDbType());

            String autoInc = isSQLite ? "AUTOINCREMENT" : "AUTO_INCREMENT";
            String currentTimestamp = isSQLite ? "CURRENT_TIMESTAMP" : "CURRENT_TIMESTAMP";

            // 1. Users table
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS users (" +
                    "id INTEGER PRIMARY KEY " + autoInc + ", " +
                    "username VARCHAR(50) NOT NULL UNIQUE, " +
                    "password_hash VARCHAR(100) NOT NULL, " +
                    "full_name VARCHAR(100) NOT NULL, " +
                    "total_xp INTEGER DEFAULT 0, " +
                    "current_level INTEGER DEFAULT 1, " +
                    "created_at TIMESTAMP DEFAULT " + currentTimestamp + ")");

            // 2. Signs table
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS signs (" +
                    "id INTEGER PRIMARY KEY " + autoInc + ", " +
                    "symbol VARCHAR(50) NOT NULL UNIQUE, " +
                    "category VARCHAR(30) NOT NULL, " +
                    "description TEXT NOT NULL, " +
                    "gesture_type VARCHAR(50) NOT NULL, " +
                    "hint TEXT)");

            // 3. Quiz Results table
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS quiz_results (" +
                    "id INTEGER PRIMARY KEY " + autoInc + ", " +
                    "user_id INTEGER NOT NULL, " +
                    "category VARCHAR(30) NOT NULL, " +
                    "score INTEGER NOT NULL, " +
                    "total_questions INTEGER NOT NULL, " +
                    "time_taken_seconds INTEGER NOT NULL, " +
                    "attempted_at TIMESTAMP DEFAULT " + currentTimestamp + ")");

            // 4. Game Scores table
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS game_scores (" +
                    "id INTEGER PRIMARY KEY " + autoInc + ", " +
                    "user_id INTEGER NOT NULL, " +
                    "game_name VARCHAR(50) NOT NULL, " +
                    "score INTEGER NOT NULL, " +
                    "moves INTEGER DEFAULT 0, " +
                    "time_taken_seconds INTEGER NOT NULL, " +
                    "played_at TIMESTAMP DEFAULT " + currentTimestamp + ")");

            // 5. User Badges table
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS user_badges (" +
                    "id INTEGER PRIMARY KEY " + autoInc + ", " +
                    "user_id INTEGER NOT NULL, " +
                    "badge_key VARCHAR(50) NOT NULL, " +
                    "unlocked_at TIMESTAMP DEFAULT " + currentTimestamp + ", " +
                    "UNIQUE(user_id, badge_key))");

            System.out.println("[DatabaseManager] All relational tables verified successfully.");

            // Seed initial data if table is empty
            seedInitialData(conn);

        } catch (SQLException e) {
            System.err.println("[DatabaseManager] Schema initialization error: " + e.getMessage());
        }
    }

    private static void seedInitialData(Connection conn) throws SQLException {
        // Check if signs exist
        try (Statement checkStmt = conn.createStatement();
             ResultSet rs = checkStmt.executeQuery("SELECT COUNT(*) FROM signs")) {
            if (rs.next() && rs.getInt(1) == 0) {
                System.out.println("[DatabaseManager] Seeding standard Sign Language catalog into database...");
                seedSigns(conn);
            }
        }

        // Check if default user exists
        try (Statement checkStmt = conn.createStatement();
             ResultSet rs = checkStmt.executeQuery("SELECT COUNT(*) FROM users")) {
            if (rs.next() && rs.getInt(1) == 0) {
                try (Statement insertStmt = conn.createStatement()) {
                    insertStmt.executeUpdate("INSERT INTO users (username, password_hash, full_name, total_xp, current_level) " +
                            "VALUES ('aayushi', 'pass123', 'Aayushi', 280, 2)");
                    insertStmt.executeUpdate("INSERT INTO users (username, password_hash, full_name, total_xp, current_level) " +
                            "VALUES ('student', 'pass123', 'Aayushi', 280, 2)");
                    System.out.println("[DatabaseManager] Default account created: username='aayushi' (and 'student'), password='pass123', name='Aayushi'");
                }
            }
        }
    }

    private static void seedSigns(Connection conn) throws SQLException {
        String[][] signsData = {
            // Alphabets (A-Z)
            {"A", "Alphabet", "Fist made with thumb pointing upward and resting flat along the side of index finger.", "ALPHA_A", "Rest thumb along index side"},
            {"B", "Alphabet", "Open palm facing forward with four straight upright fingers and thumb tucked across palm.", "ALPHA_B", "Four fingers up, thumb flat across palm"},
            {"C", "Alphabet", "Hand curved in a smooth cup shape resembling the letter 'C'.", "ALPHA_C", "Curve hand like a teacup handle"},
            {"D", "Alphabet", "Index finger pointing straight up while thumb touches tips of other curled fingers.", "ALPHA_D", "Index up, other fingers meet thumb"},
            {"E", "Alphabet", "All fingers curled down resting against the thumb, palm facing outward.", "ALPHA_E", "Curled claw fingers over thumb"},
            {"F", "Alphabet", "Index finger and thumb touch to form an 'OK' circle; remaining three fingers spread up.", "ALPHA_F", "Like an 'OK' hand gesture"},
            {"G", "Alphabet", "Index finger and thumb extended horizontally parallel to each other, pointing left.", "ALPHA_G", "Pinching horizontally"},
            {"H", "Alphabet", "Index and middle fingers extended together horizontally pointing sideways.", "ALPHA_H", "Two fingers pointing sideways"},
            {"I", "Alphabet", "Pinky finger standing straight up; remaining fingers curled into a fist with thumb across.", "ALPHA_I", "Pinky finger standing up"},
            {"J", "Alphabet", "Pinky finger starts upright and traces the curved hook of the letter 'J' in the air.", "ALPHA_J", "Trace 'J' with pinky finger"},
            {"K", "Alphabet", "Index finger points straight up, middle finger angles forward, thumb rests between them.", "ALPHA_K", "Peace sign with thumb tucked between"},
            {"L", "Alphabet", "Thumb and index finger held at 90 degrees forming an explicit 'L' shape.", "ALPHA_L", "Crisp right-angle 'L' shape"},
            {"M", "Alphabet", "Thumb tucked under first three curled fingers (index, middle, ring).", "ALPHA_M", "Three knuckles over thumb"},
            {"N", "Alphabet", "Thumb tucked under first two curled fingers (index, middle).", "ALPHA_N", "Two knuckles over thumb"},
            {"O", "Alphabet", "All fingers curved smoothly to touch the thumb, forming an 'O' ring.", "ALPHA_O", "Fingertips meet thumb forming O"},
            {"P", "Alphabet", "Hand shaped like 'K' inverted downward with index pointing down and middle angled.", "ALPHA_P", "Inverted 'K' pointing downward"},
            {"Q", "Alphabet", "Hand shaped like 'G' pointing downwards like picking a small object.", "ALPHA_Q", "G gesture pointing down"},
            {"R", "Alphabet", "Index and middle fingers crossed tightly over each other, like wishing for good luck.", "ALPHA_R", "Crossed fingers for luck"},
            {"S", "Alphabet", "Tight fist with thumb wrapped across all the other fingers.", "ALPHA_S", "Full fist with thumb across fingers"},
            {"T", "Alphabet", "Thumb tucked directly between index and middle fingers of a closed fist.", "ALPHA_T", "Thumb peeking between index and middle"},
            {"U", "Alphabet", "Index and middle fingers extended straight up together side-by-side.", "ALPHA_U", "Two fingers together pointing up"},
            {"V", "Alphabet", "Index and middle fingers extended upward spread apart in a victory 'V' shape.", "ALPHA_V", "Victory or peace sign"},
            {"W", "Alphabet", "Index, middle, and ring fingers extended upward in a spread 'W' shape.", "ALPHA_W", "Three fingers up forming 'W'"},
            {"X", "Alphabet", "Index finger bent like a hook or pirate's peg while other fingers curl into a fist.", "ALPHA_X", "Index bent into a hook"},
            {"Y", "Alphabet", "Thumb and pinky extended out wide, middle three fingers curled down (Hang Loose / Shaka).", "ALPHA_Y", "Thumb and pinky out (Phone call gesture)"},
            {"Z", "Alphabet", "Index finger extended, drawing the zigzag pattern of 'Z' in the air.", "ALPHA_Z", "Draw letter 'Z' with index finger"},

            // Numbers (0-9)
            {"0", "Number", "All fingers and thumb curved touching tips to form an oval zero.", "NUM_0", "Fingers make an oval 0"},
            {"1", "Number", "Index finger extended straight up, other fingers in a fist.", "NUM_1", "One finger pointing up"},
            {"2", "Number", "Index and middle fingers extended up spread apart.", "NUM_2", "Two fingers up"},
            {"3", "Number", "Thumb, index, and middle fingers extended (standard ASL three).", "NUM_3", "Thumb, index, and middle extended"},
            {"4", "Number", "Four fingers extended upright, thumb tucked into palm.", "NUM_4", "Four fingers up, thumb folded"},
            {"5", "Number", "All five fingers open wide with palm facing outward.", "NUM_5", "Open high-five hand"},
            {"6", "Number", "Thumb and pinky tips touch, other three fingers extended upright.", "NUM_6", "Thumb touches pinky"},
            {"7", "Number", "Thumb and ring fingertip touch, other three fingers extended.", "NUM_7", "Thumb touches ring finger"},
            {"8", "Number", "Thumb and middle fingertip touch, other three fingers extended.", "NUM_8", "Thumb touches middle finger"},
            {"9", "Number", "Thumb and index fingertip touch, other three fingers extended (similar to F).", "NUM_9", "Thumb touches index finger"},

            // Everyday Words & Greetings
            {"HELLO", "Word", "Flat open hand starts at temple/forehead and moves outward in a respectful salute.", "WORD_HELLO", "Salute gesture from forehead"},
            {"THANK YOU", "Word", "Fingertips of flat open hand touch chin or lips and extend outward towards the person.", "WORD_THANK_YOU", "Touch chin and move toward person"},
            {"PLEASE", "Word", "Flat open hand rubs gently in a smooth circular motion over the center of the chest.", "WORD_PLEASE", "Rub circular motion over chest"},
            {"YES", "Word", "Fist held upright nodding up and down at the wrist, simulating a nodding head.", "WORD_YES", "Fist nodding up and down"},
            {"NO", "Word", "Index and middle fingers snap shut quickly against the thumb, like a talking beak.", "WORD_NO", "Snap index and middle against thumb"},
            {"HELP", "Word", "Closed fist with thumb up resting on top of a flat upward-facing palm, lifted upward.", "WORD_HELP", "Thumbs up lifted by flat hand"},
            {"SORRY", "Word", "Closed fist with thumb rubbing in small circles over the heart area.", "WORD_SORRY", "Fist rubbing circular over chest"},
            {"FRIEND", "Word", "Both index fingers hooked together and then flipped and hooked the opposite way.", "WORD_FRIEND", "Interlocking index finger hooks"},
            {"LOVE", "Word", "Both arms crossed over the chest with hands resting against opposite shoulders in a hug.", "WORD_LOVE", "Arms crossed over chest in a hug"},
            {"FAMILY", "Word", "Both hands form 'F' shapes with index and thumbs touching, circling outward together.", "WORD_FAMILY", "Two 'F' hands circle and meet"},
            {"WATER", "Word", "Hand forms a 'W' shape with three fingers up, and index finger taps the side of chin twice.", "WORD_WATER", "'W' hand tapping the chin"},
            {"PEACE", "Word", "Both hands clasp together flat, slide across palms, and open outward with peace signs.", "WORD_PEACE", "Open hands spreading outwards"}
        };

        try (java.sql.PreparedStatement pstmt = conn.prepareStatement(
                "INSERT INTO signs (symbol, category, description, gesture_type, hint) VALUES (?, ?, ?, ?, ?)")) {
            for (String[] sign : signsData) {
                pstmt.setString(1, sign[0]);
                pstmt.setString(2, sign[1]);
                pstmt.setString(3, sign[2]);
                pstmt.setString(4, sign[3]);
                pstmt.setString(5, sign[4]);
                pstmt.addBatch();
            }
            pstmt.executeBatch();
            System.out.println("[DatabaseManager] Successfully seeded " + signsData.length + " sign items into database.");
        }
    }
}
