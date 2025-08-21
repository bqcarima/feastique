package com.qinet.feastique.repository.post

import com.qinet.feastique.model.entity.post.PostLike
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface PostLikeRepository : JpaRepository<PostLike, Long> {


}