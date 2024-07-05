import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;

public class MusicPlaylistDialog extends JDialog {
    private MusicPlayerGUI musicPlayerGUI;
    private ArrayList<String> songPaths;

    public MusicPlaylistDialog(MusicPlayerGUI musicPlayerGUI) {
        super(musicPlayerGUI, "Create Playlist", true);
        this.musicPlayerGUI = musicPlayerGUI;
        this.songPaths = new ArrayList<>();
        initializeDialog();
        addComponentsToDialog();
    }

    private void initializeDialog() {
        setSize(400, 400);
        setResizable(false);
        getContentPane().setBackground(musicPlayerGUI.getFrameColor());
        setLayout(null);
        setLocationRelativeTo(musicPlayerGUI);
    }


    private void addComponentsToDialog() {
        JPanel songContainer = createSongContainer();
        add(songContainer);

        JButton addSongButton = createAddSongButton(songContainer);
        add(addSongButton);

        JButton savePlaylistButton = createSavePlaylistButton();
        add(savePlaylistButton);
    }

    private JPanel createSongContainer() {
        JPanel songContainer = new JPanel();
        songContainer.setLayout(new BoxLayout(songContainer, BoxLayout.Y_AXIS));
        songContainer.setBounds((int) (getWidth() * 0.025), 10, (int) (getWidth() * 0.90), (int) (getHeight() * 0.75));
        return songContainer;
    }

    private JButton createAddSongButton(JPanel songContainer) {
        JButton addSongButton = new JButton("Add");
        addSongButton.setBounds(60, (int) (getHeight() * 0.80), 100, 25);
        addSongButton.setFont(new Font("Dialog", Font.BOLD, 14));
        addSongButton.addActionListener(e -> addSong(songContainer));
        return addSongButton;
    }

    private void addSong(JPanel songContainer) {
        JFileChooser fileChooser = createFileChooser();
        int result = fileChooser.showOpenDialog(MusicPlaylistDialog.this);
        File selectedFile = fileChooser.getSelectedFile();

        if (result == JFileChooser.APPROVE_OPTION && selectedFile != null) {
            JLabel filePathLabel = createFilePathLabel(selectedFile.getPath());
            songPaths.add(filePathLabel.getText());
            songContainer.add(filePathLabel);
            songContainer.revalidate();
        }
    }

    private JFileChooser createFileChooser() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileFilter(new FileNameExtensionFilter("MP3", "mp3"));
        fileChooser.setCurrentDirectory(new File("src/assets"));
        return fileChooser;
    }

    private JLabel createFilePathLabel(String path) {
        JLabel filePathLabel = new JLabel(path);
        filePathLabel.setFont(new Font("Dialog", Font.BOLD, 12));
        filePathLabel.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        return filePathLabel;
    }

    private JButton createSavePlaylistButton() {
        JButton savePlaylistButton = new JButton("Save");
        savePlaylistButton.setBounds(215, (int) (getHeight() * 0.80), 100, 25);
        savePlaylistButton.setFont(new Font("Dialog", Font.BOLD, 14));
        savePlaylistButton.addActionListener(e -> savePlaylist());
        return savePlaylistButton;
    }

    private void savePlaylist() {
        try {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setCurrentDirectory(new File("src/assets"));
            int result = fileChooser.showSaveDialog(MusicPlaylistDialog.this);

            if (result == JFileChooser.APPROVE_OPTION) {
                File selectedFile = getSelectedFileWithExtension(fileChooser);
                selectedFile.createNewFile();
                writeSongPathsToFile(selectedFile);
                JOptionPane.showMessageDialog(MusicPlaylistDialog.this, "Successfully Created Playlist!");
                dispose();
            }
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    private File getSelectedFileWithExtension(JFileChooser fileChooser) {
        File selectedFile = fileChooser.getSelectedFile();
        if (!selectedFile.getName().endsWith(".txt")) {
            selectedFile = new File(selectedFile.getAbsolutePath() + ".txt");
        }
        return selectedFile;
    }

    private void writeSongPathsToFile(File file) throws Exception {
        FileWriter fileWriter = new FileWriter(file);
        BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
        for (String songPath : songPaths) {
            bufferedWriter.write(songPath + "\n");
        }
        bufferedWriter.close();
    }
}
