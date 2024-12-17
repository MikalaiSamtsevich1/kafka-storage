package com.modsen.metadatafilesservice.exception


class ZookeeperServiceException(
    message: String,
    cause: Throwable? = null
) : Exception(message, cause)
