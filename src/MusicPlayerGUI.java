import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Properties;

public class MusicPlayerGUI extends JFrame {
    private Color frameColor = Color.BLACK;
    public Color getFrameColor() {
        return frameColor;
    }

    private Color textColor = Color.WHITE;

    private MusicPlayer musicPlayer;
    private JFileChooser fileChooser;
    private JLabel songTitleLabel, songArtistLabel;
    private JPanel playbackButtonsPanel;
    private JSlider playbackSlider;
    private JSlider ratingSlider;
    private List<SongRating> ratings = new ArrayList<>();

    public MusicPlayerGUI() {
        super("Music Player");
        initializeFrame();
        initializeComponents();
    }

    private void initializeFrame() {
        setSize(400, 600);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);
        setLayout(null);
        getContentPane().setBackground(frameColor);
    }

    private void initializeComponents() {
        musicPlayer = new MusicPlayer(this);
        fileChooser = createFileChooser();
        addComponentsToFrame();
    }

    private JFileChooser createFileChooser() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setCurrentDirectory(new File("src/assets"));
        fileChooser.setFileFilter(new FileNameExtensionFilter("MP3", "mp3"));
        return fileChooser;
    }

    private void addComponentsToFrame() {
        addToolbar();
        addSongImage();
        addSongTitle();
        addSongArtist();
        addPlaybackSlider();
        addPlaybackButtons();
        addRatingSlider();
    }

    private void addRatingSlider() {
        ratingSlider = new JSlider(JSlider.HORIZONTAL, 0, 5, 0);
        ratingSlider.setBounds(getWidth() / 2 - 300 / 2, 405, 300, 40);
        ratingSlider.setBackground(null);
        ratingSlider.setPaintLabels(true);
        ratingSlider.setPaintTicks(true);
        ratingSlider.setMajorTickSpacing(1);
        ratingSlider.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                JSlider source = (JSlider)e.getSource();
                if (!source.getValueIsAdjusting()) {
                    int rating = (int)source.getValue();
                    SongRating songRating = new SongRating(musicPlayer.getCurrentSong(), rating);
                    ratings.add(songRating);
                    saveRatings(ratings);
                }
            }
        });
        add(ratingSlider);
    }

    public void saveRatings(List<SongRating> ratings) {
        Properties properties = new Properties();
        for (SongRating rating : ratings) {
            properties.setProperty(rating.getSong().getFilePath(), String.valueOf(rating.getRating()));
        }
        try (FileOutputStream fos = new FileOutputStream("ratings.properties")) {
            properties.store(fos, null);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void addToolbar() {
        JToolBar toolBar = createToolbar();
        add(toolBar);
    }

    private JToolBar createToolbar() {
        JToolBar toolBar = new JToolBar();
        toolBar.setBounds(0, 0, getWidth(), 20);
        toolBar.setFloatable(false);
        toolBar.add(createMenuBar());
        return toolBar;
    }

    private JMenuBar createMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        menuBar.add(createSongMenu());
        menuBar.add(createPlaylistMenu());
        menuBar.add(createShuffleMenu());
        menuBar.add(createRepeatMenu());
        menuBar.add(createThemeMenu());  // Add the theme menu
        return menuBar;
    }

    private JMenu createSongMenu() {
        JMenu songMenu = new JMenu("Song");
        JMenuItem loadSong = new JMenuItem("Load Song");
        loadSong.addActionListener(e -> loadSong());
        songMenu.add(loadSong);
        return songMenu;
    }

    private void loadSong() {
        int result = fileChooser.showOpenDialog(MusicPlayerGUI.this);
        File selectedFile = fileChooser.getSelectedFile();

        if (result == JFileChooser.APPROVE_OPTION && selectedFile != null) {
            Song song = new Song(selectedFile.getPath());
            musicPlayer.loadSong(song);
            updateSongTitleAndArtist(song);
            updatePlaybackSlider(song);
            enablePauseButtonDisablePlayButton();
        }
    }

    private JMenu createPlaylistMenu() {
        JMenu playlistMenu = new JMenu("Playlist");
        JMenuItem createPlaylist = new JMenuItem("Create Playlist");
        createPlaylist.addActionListener(e -> new MusicPlaylistDialog(MusicPlayerGUI.this).setVisible(true));
        playlistMenu.add(createPlaylist);

        JMenuItem loadPlaylist = new JMenuItem("Load Playlist");
        loadPlaylist.addActionListener(e -> loadPlaylist());
        playlistMenu.add(loadPlaylist);
        return playlistMenu;
    }

    private void loadPlaylist() {
        JFileChooser jFileChooser = new JFileChooser();
        jFileChooser.setFileFilter(new FileNameExtensionFilter("Playlist", "txt"));
        jFileChooser.setCurrentDirectory(new File("src/assets"));

        int result = jFileChooser.showOpenDialog(MusicPlayerGUI.this);
        File selectedFile = jFileChooser.getSelectedFile();

        if(result == JFileChooser.APPROVE_OPTION && selectedFile != null){
            // Check if a song is currently playing and stop it
            if(musicPlayer != null && musicPlayer.getCurrentSong() != null) {
                musicPlayer.stopSong();
            }
            musicPlayer.loadPlaylist(selectedFile);
        }
    }

    private JMenu createShuffleMenu() {
        JMenu shuffleMenu = new JMenu("Shuffle");
        JMenuItem shufflePlaylist = new JMenuItem("Shuffle Playlist");
        shufflePlaylist.addActionListener(e -> musicPlayer.shufflePlaylist());
        shuffleMenu.add(shufflePlaylist);
        return shuffleMenu;
    }

    private JMenu createRepeatMenu() {
        JMenu repeatMenu = new JMenu("Repeat");
        JMenuItem repeatOn = new JMenuItem("Repeat On");
        repeatOn.addActionListener(e -> musicPlayer.setRepeat(true));
        repeatMenu.add(repeatOn);

        JMenuItem repeatOff = new JMenuItem("Repeat Off");
        repeatOff.addActionListener(e -> musicPlayer.setRepeat(false));
        repeatMenu.add(repeatOff);
        return repeatMenu;
    }

    private JMenu createThemeMenu() {
        JMenu themeMenu = new JMenu("Theme");
        JMenuItem theme1 = new JMenuItem("Theme 1");
        theme1.addActionListener(e -> changeTheme(new Color[]{Color.BLACK, Color.WHITE}));
        themeMenu.add(theme1);

        JMenuItem theme2 = new JMenuItem("Theme 2");
        theme2.addActionListener(e -> changeTheme(new Color[]{Color.BLUE, Color.YELLOW}));
        themeMenu.add(theme2);

        JMenuItem theme3 = new JMenuItem("Theme 3");
        theme3.addActionListener(e -> changeTheme(new Color[]{Color.GREEN, Color.BLACK}));
        themeMenu.add(theme3);

        JMenuItem theme4 = new JMenuItem("Theme 4");
        theme4.addActionListener(e -> changeTheme(new Color[]{Color.WHITE, Color.BLACK}));
        themeMenu.add(theme4);

        JMenuItem theme5 = new JMenuItem("Theme 5");
        theme5.addActionListener(e -> changeTheme(new Color[]{Color.RED, Color.WHITE}));
        themeMenu.add(theme5);

        return themeMenu;
    }

    private void addSongImage() {
        JLabel songImage = new JLabel(loadImage("src/assets/record.png"));
        songImage.setBounds(0, 50, getWidth() - 20, 225);
        add(songImage);
    }

    private void addSongTitle() {
        songTitleLabel = new JLabel("Song Title");
        songTitleLabel.setBounds(0, 285, getWidth() - 10, 30);
        songTitleLabel.setFont(new Font("Dialog", Font.BOLD, 24));
        songTitleLabel.setForeground(textColor);
        songTitleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        add(songTitleLabel);
    }

    private void addSongArtist() {
        songArtistLabel = new JLabel("Artist");
        songArtistLabel.setBounds(0, 315, getWidth() - 10, 30);
        songArtistLabel.setFont(new Font("Dialog", Font.PLAIN, 24));
        songArtistLabel.setForeground(textColor);
        songArtistLabel.setHorizontalAlignment(SwingConstants.CENTER);
        add(songArtistLabel);
    }

    private void addPlaybackSlider() {
        playbackSlider = new JSlider(JSlider.HORIZONTAL, 0, 100, 0);
        playbackSlider.setBounds(getWidth() / 2 - 300 / 2, 365, 300, 40);
        playbackSlider.setBackground(null);
        playbackSlider.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                musicPlayer.pauseSong();
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                JSlider source = (JSlider) e.getSource();
                int frame = source.getValue();
                musicPlayer.setCurrentFrame(frame);
                musicPlayer.setCurrentTimeInMilli((int) (frame / (2.08 * musicPlayer.getCurrentSong().getFrameRatePerMilliseconds())));
                musicPlayer.playCurrentSong();
                enablePauseButtonDisablePlayButton();
            }
        });
        add(playbackSlider);
    }

    private void addPlaybackButtons() {
        playbackButtonsPanel = new JPanel();
        playbackButtonsPanel.setBounds(0, 435, getWidth() - 10, 80);
        playbackButtonsPanel.setBackground(frameColor);

        JButton prevButton = createPrevButton();
        playbackButtonsPanel.add(prevButton);

        JButton playButton = createPlayButton();
        playbackButtonsPanel.add(playButton);

        JButton pauseButton = createPauseButton();
        playbackButtonsPanel.add(pauseButton);

        JButton nextButton = createNextButton();
        playbackButtonsPanel.add(nextButton);

        add(playbackButtonsPanel);
    }

    private JButton createPrevButton() {
        JButton prevButton = new JButton(loadImage("src/assets/previous.png"));
        prevButton.setBorderPainted(false);
        prevButton.setBackground(null);
        prevButton.addActionListener(e -> musicPlayer.prevSong());
        return prevButton;
    }

    private JButton createPlayButton() {
        JButton playButton = new JButton(loadImage("src/assets/play.png"));
        playButton.setBorderPainted(false);
        playButton.setBackground(null);
        playButton.addActionListener(e -> {
            enablePauseButtonDisablePlayButton();
            musicPlayer.playCurrentSong();
        });
        return playButton;
    }

    private JButton createPauseButton() {
        JButton pauseButton = new JButton(loadImage("src/assets/pause.png"));
        pauseButton.setBorderPainted(false);
        pauseButton.setBackground(null);
        pauseButton.setVisible(false);
        pauseButton.addActionListener(e -> {
            enablePlayButtonDisablePauseButton();
            musicPlayer.pauseSong();
        });
        return pauseButton;
    }

    private JButton createNextButton() {
        JButton nextButton = new JButton(loadImage("src/assets/next.png"));
        nextButton.setBorderPainted(false);
        nextButton.setBackground(null);
        nextButton.addActionListener(e -> musicPlayer.nextSong());
        return nextButton;
    }

    public void setPlaybackSliderValue(int frame) {
        playbackSlider.setValue(frame);
    }

    public void updateSongTitleAndArtist(Song song) {
        songTitleLabel.setText(song.getSongTitle());
        songArtistLabel.setText(song.getSongArtist());
    }

    public void updatePlaybackSlider(Song song) {
        playbackSlider.setMaximum(song.getMp3File().getFrameCount());
        Hashtable<Integer, JLabel> labelTable = new Hashtable<>();
        JLabel labelBeginning = new JLabel("00:00");
        labelBeginning.setFont(new Font("Dialog", Font.BOLD, 18));
        labelBeginning.setForeground(textColor);
        JLabel labelEnd = new JLabel(song.getSongLength());
        labelEnd.setFont(new Font("Dialog", Font.BOLD, 18));
        labelEnd.setForeground(textColor);
        labelTable.put(0, labelBeginning);
        labelTable.put(song.getMp3File().getFrameCount(), labelEnd);
        playbackSlider.setLabelTable(labelTable);
        playbackSlider.setPaintLabels(true);
    }

    public void enablePauseButtonDisablePlayButton() {
        JButton playButton = (JButton) playbackButtonsPanel.getComponent(1);
        JButton pauseButton = (JButton) playbackButtonsPanel.getComponent(2);
        playButton.setVisible(false);
        playButton.setEnabled(false);
        pauseButton.setVisible(true);
        pauseButton.setEnabled(true);
    }

    public void enablePlayButtonDisablePauseButton() {
        JButton playButton = (JButton) playbackButtonsPanel.getComponent(1);
        JButton pauseButton = (JButton) playbackButtonsPanel.getComponent(2);
        playButton.setVisible(true);
        playButton.setEnabled(true);
        pauseButton.setVisible(false);
        pauseButton.setEnabled(false);
    }

    private ImageIcon loadImage(String imagePath) {
        try {
            return new ImageIcon(ImageIO.read(new File(imagePath)));
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private void changeTheme(Color[] theme) {
        frameColor = theme[0];
        textColor = theme[1];

        getContentPane().setBackground(frameColor);
        songTitleLabel.setForeground(textColor);
        songArtistLabel.setForeground(textColor);
        playbackButtonsPanel.setBackground(frameColor);

        for (Component component : playbackButtonsPanel.getComponents()) {
            if (component instanceof JButton) {
                ((JButton) component).setForeground(textColor);
            }
        }

        this.revalidate();
        this.repaint();
    }
}
