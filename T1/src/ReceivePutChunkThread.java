import java.util.Random;
import java.util.concurrent.TimeUnit;

public class ReceivePutChunkThread implements Runnable {
	
	private String[] header;
	private String chunkContent;
	private Peer peer;
	
	public ReceivePutChunkThread(String[] header, String chunkContent, Peer peer) {
		this.header = header;
		this.chunkContent = chunkContent;
	}

	@Override
	public void run() {
		int senderId = Utils.asciiToNumber(header[2]);
		String fileId = Utils.fileIdToAscii(header[3]);
		int chunkNo = Utils.asciiToNumber(header[4]);
		int replicationDegree = Utils.asciiToNumber(header[5]);
		
		// A peer cant store the chunks of its own files
		if(peer.getId() == senderId) {
			return;
		}
		
		// If this peer already stored this chunk
		if(peer.getStorage().contains(header)) {
			return;
		}
		
		String stored = peer.buildStoredMessage(peer.getVersion(), peer.getId(), fileId, chunkNo);
		peer.getStorage().addChunk(new Chunk(fileId, chunkNo, chunkContent.getBytes(),chunkContent.length()), 0);
		
		Random random = new Random();
		int interval = random.nextInt(401);
		
		peer.getScheduler().schedule(new MessageSenderThread(stored,"MC", peer), interval, TimeUnit.MILLISECONDS);
	}

}