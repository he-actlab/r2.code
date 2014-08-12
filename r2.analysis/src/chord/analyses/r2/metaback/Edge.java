package chord.analyses.r2.metaback;

import chord.project.analyses.rhs.IEdge;

public class Edge implements IEdge {
	AbstractState srcNode;
	AbstractState dstNode;
	
	public Edge(AbstractState src, AbstractState dst){
		this.srcNode = src;
		this.dstNode = dst;
	}
	
	@Override
	public int canMerge(IEdge edge, boolean mustMerge) {
		Edge other = (Edge)edge;
		if(!srcNode.equals(other.srcNode))
			return -1;
		AbstractState otherDst = other.dstNode;
		if(dstNode.equals(otherDst))
			return 0;
		if(dstNode.contains(otherDst))
			return 1;
		if(otherDst.contains(dstNode))
			return 2;
		return -1;
	}

	@Override
	public boolean mergeWith(IEdge edge) {
		Edge other = (Edge)edge;
		if(dstNode.contains(other.dstNode))
			return false;
		if(other.dstNode.contains(dstNode)){
			this.dstNode = other.dstNode;
			return true;
		}
		throw new RuntimeException("Should not reach here.");
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((dstNode == null) ? 0 : dstNode.hashCode());
		result = prime * result + ((srcNode == null) ? 0 : srcNode.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Edge other = (Edge) obj;
		if (dstNode == null) {
			if (other.dstNode != null)
				return false;
		} else if (!dstNode.equals(other.dstNode))
			return false;
		if (srcNode == null) {
			if (other.srcNode != null)
				return false;
		} else if (!srcNode.equals(other.srcNode))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "Edge [srcNode=" + srcNode + ", dstNode=" + dstNode + "]";
	}

	
}

