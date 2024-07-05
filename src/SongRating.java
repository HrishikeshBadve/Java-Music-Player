public class SongRating {
    private Song song;
    private int rating; // You can make this a scale of 1-5 or 1-10, for example

    public SongRating(Song song, int rating) {
        this.song = song;
        this.rating = rating;
    }

    public Song getSong() {
        return song;
    }

    public int getRating() {
        return rating;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }
}
