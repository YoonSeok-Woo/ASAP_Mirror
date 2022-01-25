package com.ssafy.db.entity;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "study")
@Getter
@Setter
public class Study {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	int studyno;
	@Column
	String category;
	@Column
	String description;
	@Column
	int memberno;
	@Column
	String maker;
	@Column
	String studyname;
	@Column
	String image;
	@Column
	String interests;
	@Column
	String timestamp;
	
	@OneToMany(mappedBy = "study_member")
	List<StudyMember> studyMemberList = new ArrayList<>();
}

