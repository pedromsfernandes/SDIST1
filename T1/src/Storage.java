import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

public class Storage {

	private ConcurrentHashMap<Chunk, Integer> chunks;
	private ArrayList<StoredFile> storedFiles;
	private int peerId;

	public Storage(int peerId) {
		this.chunks = new ConcurrentHashMap<Chunk, Integer>();
		this.storedFiles = new ArrayList<StoredFile>();

		this.peerId = peerId;
	}

	public Storage(int peerId, ConcurrentHashMap<Chunk, Integer> chunks) {
		this.chunks = chunks;
		this.storedFiles = new ArrayList<StoredFile>();

		this.peerId = peerId;
	}

	public ConcurrentHashMap<Chunk, Integer> getChunks() {
		return chunks;
	}

	public String getChunksInfo(){
		String info = "";

		for(Map.Entry<Chunk, Integer> entry : chunks.entrySet()){
			info+=entry.getKey().toString() + "\nPerceived replication degree: " + entry.getValue() + '\n';
		}

		return info;
	}

	public boolean hasFile(String fileId) {
		for (StoredFile storedFile : storedFiles) {
			if (storedFile.getFileId() == fileId)
				return true;
		}

		return false;
	}

	public void addChunk(Chunk chunk) {
		this.chunks.put(chunk, 1);
	}

	public Chunk getChunk(String fileId, int chunkNo) {
		for (Map.Entry<Chunk, Integer> entry : chunks.entrySet()) {
			Chunk key = entry.getKey();
			if (key.getChunkNo() == chunkNo && key.getFileId().equals(fileId)) {
				return key;
			}
		}

		return null;
	}

	public Chunk getChunksFromFile(String fileId, int chunkNo) {
		for (Map.Entry<Chunk, Integer> entry : chunks.entrySet()) {
			Chunk key = entry.getKey();
			if (key.getChunkNo() == chunkNo && key.getFileId().equals(fileId)) {
				return key;
			}
		}

		return null;
	}

	public int getReplicationDegree(String fileId, int chunkNo) {
		for (Map.Entry<Chunk, Integer> entry : chunks.entrySet()) {
			Chunk key = entry.getKey();
			if (key.getChunkNo() == chunkNo && key.getFileId().equals(fileId)) {
				return entry.getValue();
			}
		}

		return -1;
	}

	public boolean contains(String fileId, int chunkNo) {
		for (Map.Entry<Chunk, Integer> entry : chunks.entrySet()) {
			Chunk key = entry.getKey();
			if (key.getChunkNo() == chunkNo && key.getFileId().equals(fileId)) {
				return true;
			}
		}

		return false;
	}

	public void updateNumConfirmationMessages(String fileId, int chunkNo) {
		for (Map.Entry<Chunk, Integer> entry : chunks.entrySet()) {
			Chunk key = entry.getKey();
			if (key.getChunkNo() == chunkNo && key.getFileId().equals(fileId)) {
				chunks.replace(key, entry.getValue() + 1);
				return;
			}
		}
	}

	public boolean decrementReplicationDegree(String fileId, int chunkNo) {

		for (Map.Entry<Chunk, Integer> entry : chunks.entrySet()) {
			if (entry.getKey().getFileId() == fileId && entry.getKey().getChunkNo() == chunkNo) {
				Chunk chunk = entry.getKey();
				int value = entry.getValue();

				chunks.replace(chunk, value);
				return true;
			}
		}

		return false;
	}

	public void addFile(StoredFile file) {
		this.storedFiles.add(file);
	}

	public void save() {

		String path = "peer" + peerId;

		File directory = new File(path);
		File backup = new File(path.concat("/backup"));
		File restore = new File(path.concat("/restore"));

		if(!directory.exists()){
			directory.mkdir();
			backup.mkdir();
			restore.mkdir();
		}

		for(Chunk entry : chunks.keySet()){
			String fileId = entry.getFileId();

			File fileChunksDir = new File(path.concat("/backup/" + fileId));

			if(!fileChunksDir.exists()){
				fileChunksDir.mkdir();
			}

			entry.serialize(path.concat("/backup/" + fileId));
		}
	}
	
	public static Storage readStorage(String path, int peerId){
		ConcurrentHashMap<Chunk, Integer> chunks = new ConcurrentHashMap<Chunk, Integer>();
		
		File folder = new File(path + "/peer" + peerId);
		File backup = new File(path + "/peer" + peerId + "/backup");

		for(File fileEntry : backup.listFiles()){
			for(File entry : fileEntry.listFiles()){
				Chunk chunk = Chunk.deserialize(entry.getPath());
				// TODO: o que fazer com replication degree
				chunks.put(chunk, 1);
			}
		}
		
		return new Storage(peerId, chunks); 
	}

	/**
	 * @param chunks the chunks to set
	 */
	public void setChunks(ConcurrentHashMap<Chunk, Integer> chunks) {
		this.chunks = chunks;
	}

	/**
	 * @return the storedFiles
	 */
	public ArrayList<StoredFile> getStoredFiles() {
		return storedFiles;
	}

	/**
	 * @param storedFiles the storedFiles to set
	 */
	public void setStoredFiles(ArrayList<StoredFile> storedFiles) {
		this.storedFiles = storedFiles;
	}

	/**
	 * @return the peerId
	 */
	public int getPeerId() {
		return peerId;
	}

	/**
	 * @param peerId the peerId to set
	 */
	public void setPeerId(int peerId) {
		this.peerId = peerId;
	}

	public void deleteChunks(String fileId) {
		chunks.entrySet().removeIf(entry -> entry.getKey().getFileId() == fileId);
	}
}
