package us.axefan.demo;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import com.avaje.ebean.validation.NotNull;

@Entity()
@Table(name="DatabaseEntries")
public class DatabaseEntry {
	
    @Id
    private int id;
    
    @NotNull
    private String playerName;
    
	public void setId(int id) {
		this.id = id;
	}

	public int getId() {
		return id;
	}

	public void setPlayerName(String playerName) {
		this.playerName = playerName;
	}

	public String getPlayerName() {
		return playerName;
	}
	
}
