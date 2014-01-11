package com.pmease.gitop.model.gatekeeper;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.pmease.commons.editable.annotation.Editable;
import com.pmease.commons.editable.annotation.TableLayout;
import com.pmease.gitop.model.gatekeeper.checkresult.Accepted;
import com.pmease.gitop.model.gatekeeper.checkresult.CheckResult;
import com.pmease.gitop.model.gatekeeper.checkresult.Pending;
import com.pmease.gitop.model.gatekeeper.checkresult.PendingAndBlock;
import com.pmease.gitop.model.gatekeeper.checkresult.Rejected;
import com.pmease.gitop.model.gatekeeper.voteeligibility.VoteEligibility;

@SuppressWarnings("serial")
@Editable(name="If Any Of Contained Gate Keepers Is Passed", order=200, icon="icon-servers",  
		description="This gate keeper will be passed if any of the contained gate keepers is passed.")
@TableLayout
public class OrGateKeeper extends AndOrGateKeeper {

	@Override
	protected CheckResult aggregate(Checker checker) {
		List<String> pendingReasons = new ArrayList<String>();
		List<String> rejectReasons = new ArrayList<String>();
		Collection<VoteEligibility> voteEligibilities = new ArrayList<>();
		
		for (GateKeeper each: getGateKeepers()) {
			CheckResult result = checker.check(each);
			if (result instanceof Rejected) {
				rejectReasons.addAll(result.getReasons());
			} else if (result instanceof Accepted) {
				return result;
			} else if (result instanceof PendingAndBlock) {
				result.getReasons().addAll(pendingReasons);
				result.getVoteEligibilities().addAll(voteEligibilities);
				return result;
			} else if (result instanceof Pending) {
				pendingReasons.addAll(result.getReasons());
				voteEligibilities.addAll(result.getVoteEligibilities());
			}
		}
		
		if (!pendingReasons.isEmpty())
			return pending(pendingReasons, voteEligibilities);
		else
			return rejected(rejectReasons);
	}

}
