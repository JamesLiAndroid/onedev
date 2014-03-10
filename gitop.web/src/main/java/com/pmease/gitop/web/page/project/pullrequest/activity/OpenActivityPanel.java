package com.pmease.gitop.web.page.project.pullrequest.activity;

import org.apache.commons.lang3.StringUtils;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.basic.MultiLineLabel;
import org.apache.wicket.markup.html.form.TextArea;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import com.pmease.gitop.core.Gitop;
import com.pmease.gitop.core.manager.AuthorizationManager;
import com.pmease.gitop.core.manager.PullRequestManager;
import com.pmease.gitop.model.PullRequest;
import com.pmease.gitop.model.User;
import com.pmease.gitop.web.component.link.GitPersonLink;
import com.pmease.gitop.web.page.project.api.GitPerson;
import com.pmease.gitop.web.util.DateUtils;

@SuppressWarnings("serial")
public class OpenActivityPanel extends Panel {

	private String description;
	
	public OpenActivityPanel(String id, IModel<PullRequest> model) {
		super(id, model);
	}
	
	private Fragment renderForView() {
		Fragment fragment = new Fragment("body", "viewFrag", this);

		description = getPullRequest().getDescription();
		if (StringUtils.isNotBlank(description))
			fragment.add(new MultiLineLabel("comment", description));
		else
			fragment.add(new Label("comment", "<i>No description</i>").setEscapeModelStrings(false));
		
		return fragment;
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		PullRequest request = getPullRequest();
		User submittedBy = request.getSubmittedBy();
		if (submittedBy != null) {
			GitPerson person = new GitPerson(submittedBy.getName(), submittedBy.getEmail());
			add(new GitPersonLink("user", Model.of(person), GitPersonLink.Mode.FULL));
		} else {
			add(new Label("<i>Unknown</i>").setEscapeModelStrings(false));
		}
		
		add(new Label("date", DateUtils.formatAge(request.getCreateDate())));
		
		add(new AjaxLink<Void>("edit") {

			@Override
			public void onClick(AjaxRequestTarget target) {
				description = getPullRequest().getDescription();
				
				Fragment fragment = new Fragment("body", "editFrag", OpenActivityPanel.this);
				
				final TextArea<String> commentArea = new TextArea<String>("comment", new IModel<String>() {

					@Override
					public void detach() {
					}

					@Override
					public String getObject() {
						return description;
					}

					@Override
					public void setObject(String object) {
						description = object;
					}

				});
				
				commentArea.add(new AjaxFormComponentUpdatingBehavior("blur") {

					@Override
					protected void onUpdate(AjaxRequestTarget target) {
						commentArea.processInput();
					}
					
				});
				
				fragment.add(commentArea);
				
				fragment.add(new AjaxLink<Void>("save") {

					@Override
					public void onClick(AjaxRequestTarget target) {
						PullRequest request = getPullRequest();
						request.setDescription(description);
						Gitop.getInstance(PullRequestManager.class).save(request);

						Fragment fragment = renderForView();
						OpenActivityPanel.this.replace(fragment);
						target.add(OpenActivityPanel.this);
					}
					
				});
				
				fragment.add(new AjaxLink<Void>("cancel") {

					@Override
					public void onClick(AjaxRequestTarget target) {
						Fragment fragment = renderForView();
						OpenActivityPanel.this.replace(fragment);
						target.add(OpenActivityPanel.this);
					}
					
				});
				
				OpenActivityPanel.this.replace(fragment);
				
				target.add(OpenActivityPanel.this);
			}

			@Override
			protected void onConfigure() {
				super.onConfigure();
				
				setVisible(Gitop.getInstance(AuthorizationManager.class)
						.canModify(getPullRequest()));
			}

		});
		
		add(renderForView());

		setOutputMarkupId(true);
	}

	private PullRequest getPullRequest() {
		return (PullRequest) getDefaultModelObject();
	}
	
}
