package amazonTests;

public interface CloudService {

	NodeDetails leaseNode(Configurations config);

	void releaseNode(NodeDetails node);
}
